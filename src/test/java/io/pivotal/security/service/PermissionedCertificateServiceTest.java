package io.pivotal.security.service;

import io.pivotal.security.auth.UserContext;
import io.pivotal.security.auth.UserContextHolder;
import io.pivotal.security.credential.CertificateCredentialValue;
import io.pivotal.security.data.CertificateDataService;
import io.pivotal.security.domain.CertificateCredentialVersion;
import io.pivotal.security.domain.CredentialVersion;
import io.pivotal.security.entity.Credential;
import io.pivotal.security.exceptions.ParameterizedValidationException;
import io.pivotal.security.request.GenerationParameters;
import io.pivotal.security.request.PermissionOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionedCertificateServiceTest {
  private PermissionedCertificateService subject;
  private PermissionedCredentialService permissionedCredentialService;
  private CertificateDataService certificateDataService;
  private PermissionCheckingService permissionCheckingService;
  private UserContextHolder userContextHolder;

  @Before
  public void beforeEach() {
    permissionedCredentialService = mock(PermissionedCredentialService.class);
    certificateDataService = mock(CertificateDataService.class);
    permissionCheckingService = mock(PermissionCheckingService.class);
    certificateDataService = mock(CertificateDataService.class);
    userContextHolder = mock(UserContextHolder.class);
    subject = new PermissionedCertificateService(permissionedCredentialService, certificateDataService, permissionCheckingService, userContextHolder);
  }

  @Test
  public void save_whenTransitionalIsFalse_delegatesToPermissionedCredentialService() throws Exception {
    CertificateCredentialValue value = mock(CertificateCredentialValue.class);
    when(value.isTransitional()).thenReturn(false);
    subject.save(
        mock(CredentialVersion.class),
        "/some-name",
        value,
        mock(GenerationParameters.class),
        newArrayList(),
        "overwrite",
        newArrayList()
    );
    Mockito.verify(permissionedCredentialService).save(any(),
        eq("/some-name"),
        eq("certificate"),
        eq(value),
        any(),
        any(),
        eq("overwrite"),
        any()
    );
  }

  @Test
  public void save_whenTransitionalIsTrue_andThereAreNoOtherTransitionalVersions_delegatesToPermissionedCredentialService() throws Exception {
    CertificateCredentialValue value = mock(CertificateCredentialValue.class);
    when(value.isTransitional()).thenReturn(true);

    CertificateCredentialVersion previousVersion = mock(CertificateCredentialVersion.class);
    when(previousVersion.isVersionTransitional()).thenReturn(false);

    when(permissionedCredentialService.findAllByName(eq("/some-name"), any()))
        .thenReturn(newArrayList(previousVersion));

    subject.save(
        mock(CredentialVersion.class),
        "/some-name",
        value,
        mock(GenerationParameters.class),
        newArrayList(),
        "overwrite",
        newArrayList()
    );
    Mockito.verify(permissionedCredentialService).save(any(),
        eq("/some-name"),
        eq("certificate"),
        eq(value),
        any(),
        any(),
        eq("overwrite"),
        any()
    );
  }

  @Test
  public void save_whenTransitionalIsTrue_AndThereIsAnotherTransitionalVersion_throwsAnException() throws Exception {
    CertificateCredentialValue value = mock(CertificateCredentialValue.class);
    when(value.isTransitional()).thenReturn(true);

    CertificateCredentialVersion previousVersion = mock(CertificateCredentialVersion.class);
    when(previousVersion.isVersionTransitional()).thenReturn(true);

    when(permissionedCredentialService.findAllByName(eq("/some-name"), any()))
        .thenReturn(newArrayList(previousVersion));

    try {
      subject.save(
          mock(CredentialVersion.class),
          "/some-name",
          value,
          mock(GenerationParameters.class),
          newArrayList(),
          "overwrite",
          newArrayList()
      );
      fail("should throw exception");
    } catch (ParameterizedValidationException e) {
      assertThat(e.getMessage(), equalTo("error.too_many_transitional_versions"));
    }
  }

  @Test
  public void getAll_returnsAllCertificatesTheCurrentUserCanAccess() throws Exception {
    Credential myCredential = mock(Credential.class);
    when(myCredential.getName()).thenReturn("my-credential");
    Credential yourCredential = mock(Credential.class);
    when(yourCredential.getName()).thenReturn("your-credential");

    UserContext userContext = mock(UserContext.class);
    when(userContextHolder.getUserContext()).thenReturn(userContext);

    String user = "my-user";
    when(userContext.getActor()).thenReturn(user);

    when(permissionCheckingService.hasPermission(user, "my-credential", PermissionOperation.READ)).thenReturn(true);
    when(permissionCheckingService.hasPermission(user, "your-credential", PermissionOperation.READ)).thenReturn(false);

    when(certificateDataService.findAll())
        .thenReturn(newArrayList(myCredential, yourCredential));

    final List<Credential> certificates = subject.getAll(newArrayList());
    assertThat(certificates, equalTo(newArrayList(myCredential)));
  }
}
