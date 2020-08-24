package io.archilab.prox.projectservice.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.archilab.prox.projectservice.project.CreatorID;
import io.archilab.prox.projectservice.project.Project;
import io.archilab.prox.projectservice.project.ProjectRepository;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

class WebSecurityTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private AuthenticationUtils authenticationUtils;

  @InjectMocks
  private WebSecurity webSecurity;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void when_projectCreator_and_user_are_equal_then_return_true() {
    UUID uuid = UUID.randomUUID();
    assertTrue(testCheckProjectCreator(uuid, uuid));
  }

  @Test
  void when_projectCreator_and_user_are_not_equal_then_return_false() {
    assertFalse(testCheckProjectCreator(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void when_projectCreator_uuid_is_null_then_return_false() {
    assertFalse(testCheckProjectCreator(UUID.randomUUID(), null));
  }

  @Test
  void when_user_uuid_is_null_then_return_false() {
    assertFalse(testCheckProjectCreator(null, UUID.randomUUID()));
  }

  private boolean testCheckProjectCreator(UUID userUUID, UUID creatorUUID) {
    Project mockProject = Mockito.mock(Project.class);
    Mockito.when(mockProject.getCreatorID()).thenReturn(new CreatorID(creatorUUID));
    Mockito.when(projectRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockProject));
    Mockito.when(authenticationUtils.getUserUUIDFromRequest(Mockito.any(HttpServletRequest.class))).thenReturn(Optional.ofNullable(userUUID));

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    return webSecurity.checkProjectCreator(request, UUID.randomUUID(), projectRepository);
  }
}