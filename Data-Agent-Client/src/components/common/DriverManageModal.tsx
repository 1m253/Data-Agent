import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '../ui/Dialog';
import { Button } from '../ui/Button';
import { driverService } from '../../services/driver.service';
import { useToast } from '../../hooks/useToast';
import { resolveErrorMessage } from '../../lib/errorMessage';
import type { InstalledDriverResponse, AvailableDriverResponse } from '../../types/driver';

interface DriverManageModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  databaseType: string;
  onSelectDriver: (driverPath: string) => void;
}

export function DriverManageModal({
  open,
  onOpenChange,
  databaseType,
  onSelectDriver,
}: DriverManageModalProps) {
  const { t } = useTranslation();
  const toast = useToast();
  const [installed, setInstalled] = useState<InstalledDriverResponse[]>([]);
  const [available, setAvailable] = useState<AvailableDriverResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [downloadingVersion, setDownloadingVersion] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !databaseType?.trim()) return;
    
    let isMounted = true;
    setLoading(true);
    
    Promise.all([
      driverService.listInstalledDrivers(databaseType),
      driverService.listAvailableDrivers(databaseType),
    ])
      .then(([inst, av]) => {
        if (isMounted) {
          setInstalled(inst);
          setAvailable(av);
        }
      })
      .catch((err) => {
        if (isMounted) {
          toast.error(resolveErrorMessage(err, t('drivers.download_failed')));
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [open, databaseType, t, toast]);

  const handleDownload = async (version: string) => {
    setDownloadingVersion(version);
    try {
      const res = await driverService.downloadDriver(databaseType, version);
      toast.success(t('drivers.download_success'));
      setInstalled((prev) => [
        ...prev,
        {
          databaseType: res.databaseType,
          fileName: res.fileName,
          version: res.version,
          filePath: res.driverPath,
          fileSize: 0,
          lastModified: new Date().toISOString(),
        },
      ]);
      setAvailable((prev) =>
        prev.map((a) => (a.version === version ? { ...a, installed: true } : a))
      );
    } catch (err) {
      toast.error(resolveErrorMessage(err, t('drivers.download_failed')));
    } finally {
      setDownloadingVersion(null);
    }
  };

  const handleDelete = async (version: string) => {
    try {
      await driverService.deleteDriver(databaseType, version);
      toast.success(t('drivers.delete_success'));
      setInstalled((prev) => prev.filter((d) => d.version !== version));
      setAvailable((prev) =>
        prev.map((a) => (a.version === version ? { ...a, installed: false } : a))
      );
    } catch (err) {
      toast.error(resolveErrorMessage(err, t('drivers.delete_failed')));
    }
  };

  const handleSelect = (filePath: string) => {
    onSelectDriver(filePath);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[560px] max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{t('drivers.title')}</DialogTitle>
          <DialogDescription>
            {t('drivers.database_type')}: {databaseType || '-'}
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-6 py-4">
          {loading ? (
            <p className="text-sm text-muted-foreground">{t('explorer.loading')}</p>
          ) : (
            <>
              <section>
                <h4 className="text-sm font-medium text-foreground mb-2">
                  {t('drivers.installed')}
                </h4>
                {installed.length === 0 ? (
                  <p className="text-sm text-muted-foreground">{t('drivers.no_installed')}</p>
                ) : (
                  <ul className="border border-border rounded-md divide-y divide-border max-h-40 overflow-y-auto">
                    {installed.map((d) => (
                      <li
                        key={d.version}
                        className="flex items-center justify-between px-3 py-2 text-sm"
                      >
                        <span className="text-foreground">
                          {d.fileName} ({d.version})
                        </span>
                        <div className="flex items-center gap-2">
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => handleSelect(d.filePath)}
                          >
                            {t('drivers.select_driver')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => handleDelete(d.version)}
                          >
                            {t('drivers.delete')}
                          </Button>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
              <section>
                <h4 className="text-sm font-medium text-foreground mb-2">
                  {t('drivers.available')}
                </h4>
                {available.length === 0 ? (
                  <p className="text-sm text-muted-foreground">{t('drivers.no_available')}</p>
                ) : (
                  <ul className="border border-border rounded-md divide-y divide-border max-h-40 overflow-y-auto">
                    {available.map((d) => (
                      <li
                        key={d.version}
                        className="flex items-center justify-between px-3 py-2 text-sm"
                      >
                        <span className="text-foreground">{d.version}</span>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={d.installed || downloadingVersion === d.version}
                          onClick={() => handleDownload(d.version)}
                        >
                          {downloadingVersion === d.version
                            ? t('drivers.downloading')
                            : d.installed
                              ? t('drivers.installed')
                              : t('drivers.download')}
                        </Button>
                      </li>
                    ))}
                  </ul>
                )}
              </section>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
