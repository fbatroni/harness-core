package io.harness.licensing.entities.modules;

import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.licensing.UpdateChannel;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.annotation.TypeAlias;

@OwnedBy(HarnessTeam.GTM)
@Value
@Builder
@EqualsAndHashCode(callSuper = true)
@Entity(value = "moduleLicenses", noClassnameStored = true)
@Persistent
@TypeAlias("io.harness.license.entities.module.CFModuleLicense")
public class CFModuleLicense extends ModuleLicense {
  private int numberOfUsers;
  private int numberOfClientMAUs;
  private List<UpdateChannel> updateChannels;
}
