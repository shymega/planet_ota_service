package uk.co.planetcom.infrastructure.ota.server.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.*;
import lombok.Data;
import uk.co.planetcom.infrastructure.ota.server.enums.AssetType;
import uk.co.planetcom.infrastructure.ota.server.enums.AssetVendor;
import uk.co.planetcom.infrastructure.ota.server.enums.UpdateChannel;

import java.io.Serializable;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AssetVO implements Serializable {
    public AssetVO(Asset o) {
        this.setAssetId(o.getAssetId());
        this.setAssetFileName(o.getAssetFileName());
        this.setAssetVendor(o.getAssetVendor());
        this.setAssetVersion(o.getAssetVersion());
        this.setAssetDownloadUri(o.getAssetDownloadUri());
        this.setAssetChangelog(o.getAssetChangelog());
        this.setAssetSha256Hash(o.getAssetSha256Hash());
        this.setReleaseTimeStamp(o.getReleaseTimeStamp());
        this.setAssetType(o.getAssetType());
        this.setUpdateChannel(o.getUpdateChannel());
        this.setAssetCompat(o.getAssetCompat());
        this.setAssetSuppressed(o.isAssetSuppressed());
        this.setUploadTimeStamp(o.getUploadTimeStamp());
    }

    private UUID assetId; /* UUID/GUID to avoid column collision */

    private String assetFileName; /* String representation of the filename that the object was uploaded as. */

    private AssetVendor assetVendor; /* Vendor of Asset. */

    private String assetVersion; /* Arbitrary String denoting the version of the Asset */

    private URI assetDownloadUri; /* A URI to the asset. */

    private List<String> assetChangelog; /* Newline delimited String of changes in this asset. */

    private String assetSha256Hash; /* SHA-256 hash of the asset, generate from bytes stored in RDBMS */

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) /* Restrict access from public API. */
    @Hidden
    private ZonedDateTime releaseTimeStamp; /* When the asset is 'due' to be released to users. */

    private AssetType assetType; /* Can be queried from the `AssetService` class. */

    private UpdateChannel updateChannel; /* Channel that the update is released on. */

    private AssetCompat assetCompat;

    @Hidden
    @JsonIgnore
    private boolean assetSuppressed; /* Whenever the asset has been suppressed, for whatever reason. */

    @JsonIgnore
    private ZonedDateTime uploadTimeStamp;

    @Transient
    @JsonIgnore
    @Hidden
    private final ZoneId timeZone = ZoneId.of(Optional.ofNullable(System.getenv("TZ"))
        .orElse("Europe/London"));

    @JsonIgnore
    @Hidden
    public boolean isAvailable() {
        return !(this.releaseTimeStamp.isAfter(ZonedDateTime.now(this.timeZone)) && !this.isAssetSuppressed());
    }

    @JsonIgnore
    @Hidden
    public boolean isNotAvailable() {
        return !isAvailable();
    }
}