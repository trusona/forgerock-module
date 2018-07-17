package com.trusona.forgerock.auth.principal;

import com.trusona.client.TrusonaClient;
import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Optional;

public class TrusonaAppPrincipalMapper implements PrincipalMapper {
  private final TrusonaClient trusonaClient;

  public TrusonaAppPrincipalMapper(TrusonaClient trusonaClient) {
    this.trusonaClient = trusonaClient;
  }

  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    return Optional.empty();
  }
}
