import { useTranslation } from 'react-i18next';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../ui/Dialog';
import { Button } from '../ui/Button';

interface DeleteConnectionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  connectionId: number | null;
  onConfirm: (id: number) => void;
  isPending: boolean;
}

export function DeleteConnectionDialog({
  open,
  onOpenChange,
  connectionId,
  onConfirm,
  isPending,
}: DeleteConnectionDialogProps) {
  const { t } = useTranslation();

  const handleConfirm = () => {
    if (connectionId != null) {
      onConfirm(connectionId);
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle>{t('connections.delete_confirm_title')}</DialogTitle>
          <DialogDescription>{t('connections.delete_confirm_desc')}</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            {t('connections.cancel')}
          </Button>
          <Button
            variant="destructive"
            disabled={isPending}
            onClick={handleConfirm}
          >
            {isPending ? t('connections.saving') : t('connections.delete')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
