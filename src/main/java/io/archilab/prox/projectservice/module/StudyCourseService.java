package io.archilab.prox.projectservice.module;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class StudyCourseService {

  private final Logger logger = LoggerFactory.getLogger(StudyCourseService.class);

  private final StudyCourseClient studyCourseClient;
  private final ModuleRepository moduleRepository;
  private final StudyCourseRepository studyCourseRepository;

  public StudyCourseService(
      StudyCourseClient studyCourseClient,
      ModuleRepository moduleRepository,
      StudyCourseRepository studyCourseRepository) {
    this.studyCourseClient = studyCourseClient;
    this.moduleRepository = moduleRepository;
    this.studyCourseRepository = studyCourseRepository;
  }

  public boolean hasData() {
    return this.studyCourseRepository.count() > 0;
  }

  public void importStudyCourses() {
    this.logger.info("Start importing Study Courses");

    List<StudyCourse> studyCourses = this.studyCourseClient.getStudyCourses();
    for (StudyCourse studyCourse : studyCourses) {
      Optional<StudyCourse> existingStudyCourseOptional =
          this.studyCourseRepository.findByExternalStudyCourseID(
              studyCourse.getExternalStudyCourseID());
      StudyCourse savedStudyCourse = null;

      if (existingStudyCourseOptional.isPresent()) {
        this.logger.info(
            "StudyCourse with ID {} already exists.", studyCourse.getExternalStudyCourseID());
        savedStudyCourse = existingStudyCourseOptional.get();
      } else {
        this.logger.info(
            "StudyCourse with ID {} does not exist yet.", studyCourse.getExternalStudyCourseID());
        savedStudyCourse = new StudyCourse();
      }

      savedStudyCourse.setName(studyCourse.getName());
      savedStudyCourse.setAcademicDegree(studyCourse.getAcademicDegree());
      savedStudyCourse.setExternalStudyCourseID(studyCourse.getExternalStudyCourseID());
      savedStudyCourse = this.studyCourseRepository.save(savedStudyCourse);

      List<Module> retrievedModules = studyCourse.getModules();
      for (Module module : retrievedModules) {
        Optional<Module> existingModuleOptional =
            this.moduleRepository.findByExternalModuleID(module.getExternalModuleID());

        if (existingModuleOptional.isPresent()) {
          this.logger.info("Module with ID {} already exists.", module.getExternalModuleID());
          Module existingModule = existingModuleOptional.get();
          existingModule.setName(module.getName());
          existingModule.setProjectType(module.getProjectType());
          existingModule.setStudyCourse(savedStudyCourse);
          this.moduleRepository.save(existingModule);
        } else {
          this.logger.info("Module with ID {} does not exist yet.", module.getExternalModuleID());
          module.setStudyCourse(savedStudyCourse);
          this.moduleRepository.save(module);
        }
      }
    }

    this.logger.info("Finished import");
  }

  public void cleanUp() {

    // delete all study courses without modules
    Iterable<StudyCourse> studyCourses = this.studyCourseRepository.findAll();

    for (StudyCourse studyCourse : studyCourses) {
      if (studyCourse.getModules().isEmpty()) {
        this.studyCourseRepository.delete(studyCourse);
      }
    }
  }
}
