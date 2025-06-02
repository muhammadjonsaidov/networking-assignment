
import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { api } from '@/lib/api';
import { ApiResponse, Order } from '@/types/api';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';

interface OrderStatusUpdate {
  newStatus: 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';
}

interface OrderStatusDialogProps {
  order: Order | null;
  isOpen: boolean;
  onClose: () => void;
}

export function OrderStatusDialog({ order, isOpen, onClose }: OrderStatusDialogProps) {
  const queryClient = useQueryClient();

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: OrderStatusUpdate }) => 
      api.put<ApiResponse<Order>>(`/orders/${id}/status`, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      onClose();
      toast.success('Order status updated successfully!');
    },
    onError: (error) => {
      console.error('Update status error:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to update order status');
    }
  });

  const { handleSubmit, setValue, control, reset } = useForm<OrderStatusUpdate>();

  const onSubmit = (data: OrderStatusUpdate) => {
    if (order?.id) {
      try {
        updateStatusMutation.mutate({ id: order.id, status: data });
      } catch (error) {
        console.error('Status update submission error:', error);
        toast.error('Failed to update order status');
      }
    }
  };

  // Set initial value when dialog opens
  useState(() => {
    if (order && isOpen) {
      setValue('newStatus', order.status);
    }
  });

  const handleClose = () => {
    reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Update Order Status</DialogTitle>
          <DialogDescription>
            Change the status of order #{order?.id}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="newStatus">New Status</Label>
            <Controller
              name="newStatus"
              control={control}
              rules={{ required: 'Status is required' }}
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select new status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="PENDING">Pending</SelectItem>
                    <SelectItem value="PROCESSING">Processing</SelectItem>
                    <SelectItem value="SHIPPED">Shipped</SelectItem>
                    <SelectItem value="DELIVERED">Delivered</SelectItem>
                    <SelectItem value="CANCELLED">Cancelled</SelectItem>
                    <SelectItem value="RETURNED">Returned</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>
          <div className="flex justify-end space-x-2">
            <Button 
              type="button" 
              variant="outline" 
              onClick={handleClose}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={updateStatusMutation.isPending}>
              {updateStatusMutation.isPending ? 'Updating...' : 'Update Status'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
