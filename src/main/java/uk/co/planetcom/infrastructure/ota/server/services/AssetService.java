package uk.co.planetcom.infrastructure.ota.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.co.planetcom.infrastructure.ota.server.db.AssetsRepository;
import uk.co.planetcom.infrastructure.ota.server.domain.Asset;
import uk.co.planetcom.infrastructure.ota.server.enums.*;
import uk.co.planetcom.infrastructure.ota.server.utils.UrlUtils;

import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public final class AssetService {
    @Autowired
    private AssetsRepository repository;

    public final Asset create(final Asset entity) throws MalformedURLException {
        entity.setAssetFileName(UrlUtils.getUrlFileName(entity.getAssetDownloadUri().toString()));
        return repository.saveAndFlush(entity);
    }

    public final void delete(final Asset entity) {
        repository.delete(entity);
    }

    public final void setNewReleaseTimestamp(final UUID id, final ZonedDateTime newReleaseTimestamp) {
        Asset asset = repository.findById(id)
            .orElseThrow(); // FIXME: handle safely.
        asset.setReleaseTimeStamp(newReleaseTimestamp);
        // Send out notification if Asset is *now* available.
        if (asset.isAvailable()) notifyDevices(asset);
        repository.saveAndFlush(asset);
    }

    private final void notifyDevices(final Asset o) {
        log.info("Asset updated, and now available. Begin fan-out notify.");
    }

    public final void suppressAsset(final UUID id) {
        modifySuppressed(id, true);
    }

    public final void deSuppressAsset(final UUID id) {
        modifySuppressed(id, false);
    }

    private final void modifySuppressed(final UUID id, final boolean suppression) {
        Asset asset = repository.findById(id)
            .orElseThrow(); // FIXME: handle safely.
        // Send out notification if Asset is *now* available.
        asset.setAssetSuppressed(suppression);

        if (asset.isAvailable()) notifyDevices(asset);
        repository.saveAndFlush(asset);
    }

    public final List<Asset> findAll() {
        return repository.findAll()
            .stream()
            .filter(Objects::nonNull)
            .filter(Asset::isAvailable)
            .toList();
    }

    public final List<Asset> findAllByVendorType(final AssetVendorEnum assetVendorEnum) {
        return repository.findAllByAssetVendor(assetVendorEnum)
            .stream()
            .filter(Objects::nonNull)
            .filter(Asset::isAvailable)
            .toList();
    }

    public final Optional<Asset> findByUuid(final UUID uuid) {
        return repository.findById(uuid)
            .stream()
            .filter(Objects::nonNull)
            .filter(Asset::isAvailable)
            .findFirst();
    }

    public final List<Asset> findAllAvailable() {
        return repository.findAll()
            .stream()
            .filter(Objects::nonNull)
            .filter(Asset::isAvailable)
            .toList();
    }

    public final List<Asset> findAllUnavailable() {
        return repository.findAll()
            .stream()
            .filter(Objects::nonNull)
            .filter(Asset::isNotAvailable)
            .toList();
    }
}
