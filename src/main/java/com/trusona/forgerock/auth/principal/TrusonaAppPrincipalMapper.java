package com.trusona.forgerock.auth.principal;

import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Optional;

public class TrusonaAppPrincipalMapper implements PrincipalMapper {
  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    return Optional.empty();
  }
}
