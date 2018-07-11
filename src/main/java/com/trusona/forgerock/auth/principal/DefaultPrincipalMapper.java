package com.trusona.forgerock.auth.principal;

import com.sun.identity.authentication.internal.AuthPrincipal;
import com.trusona.forgerock.auth.TrusonaDebug;
import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

public class DefaultPrincipalMapper implements PrincipalMapper {

  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    TrusonaDebug.getInstance().message("Mapping Result to Principal: {}", result);
    Date lastAllowedExpiration = new Date(System.currentTimeMillis() - 60 * 1000);

    return Optional.of(result)
      .filter(TrusonaficationResult::isSuccessful)
      .filter(trusonaficationResult -> trusonaficationResult.getExpiresAt().after(lastAllowedExpiration))
      .map(TrusonaficationResult::getUserIdentifier)
      .map(AuthPrincipal::new);
  }
}