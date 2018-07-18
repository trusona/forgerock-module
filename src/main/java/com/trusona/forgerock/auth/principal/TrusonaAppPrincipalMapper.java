package com.trusona.forgerock.auth.principal;

import com.sun.identity.authentication.internal.AuthPrincipal;
import com.sun.identity.idm.AMIdentity;
import com.trusona.client.TrusonaClient;
import com.trusona.client.dto.response.UserResponse;
import com.trusona.forgerock.auth.TrusonaDebug;
import com.trusona.sdk.resources.dto.TrusonaficationResult;

import java.security.Principal;
import java.util.Optional;

import static com.trusona.forgerock.auth.principal.DefaultPrincipalMapper.TRUSONA_APP_PREFIX;

public class TrusonaAppPrincipalMapper implements PrincipalMapper {
  private final TrusonaClient trusonaClient;
  private final IdentityFinder identityFinder;

  public TrusonaAppPrincipalMapper(TrusonaClient trusonaClient, IdentityFinder identityFinder) {
    this.trusonaClient = trusonaClient;
    this.identityFinder = identityFinder;
  }

  @Override
  public Optional<Principal> mapPrincipal(TrusonaficationResult result) {
    String trusonaId = result.getUserIdentifier().replace(TRUSONA_APP_PREFIX, "");
    TrusonaDebug.getInstance().message("Looking up user by trusonaId {}", trusonaId);

    Optional<UserResponse> userResponse = trusonaClient.getUser(trusonaId);

    if (userResponse.isPresent()) {
      for (String email : userResponse.get().getEmails()) {
        TrusonaDebug.getInstance().message("Looking within ForgeRock for user with email '{}'", email);
        AMIdentity identity = identityFinder.findForgeRockUser(email);

        if (identity != null) {
          TrusonaDebug.getInstance().message("User found!");
          return Optional.of(new AuthPrincipal(identity.getUniversalId()));
        }
      }

      return Optional.of(new AuthPrincipal(userResponse.get().getEmails().iterator().next()));
    }
    else {
      TrusonaDebug.getInstance().warning("Did not find user by trusonaId {}", trusonaId);
      return Optional.empty();
    }
  }
}