package com.trusona.forgerock.auth.principal;

import com.sun.identity.authentication.internal.AuthPrincipal;
import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Optional;

public class TrusonaSdkPrincipalMapper implements PrincipalMapper {
  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    return Optional.of(new AuthPrincipal(result.getUserIdentifier()));
  }
}
