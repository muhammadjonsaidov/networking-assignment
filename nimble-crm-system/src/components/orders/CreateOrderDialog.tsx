
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus } from 'lucide-react';
import { useForm, Controller } from 'react-hook-form';
import { api } from '@/lib/api';
import { ApiResponse, Page, Customer, Product, Order } from '@/types/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';

interface OrderCreateForm {
  productId: string;
  customerId: string;
  quantity: number;
}

export function CreateOrderDialog() {
  const [isOpen, setIsOpen] = useState(false);
  const queryClient = useQueryClient();

  const { data: customersData } = useQuery({
    queryKey: ['customers-for-orders'],
    queryFn: () => api.get<ApiResponse<Page<Customer>>>('/customers'),
    enabled: isOpen
  });

  const { data: productsData } = useQuery({
    queryKey: ['products-for-orders'],
    queryFn: () => api.get<ApiResponse<Page<Product>>>('/products'),
    enabled: isOpen
  });

  const createMutation = useMutation({
    mutationFn: (order: { productId: number; customerId: number; quantity: number }) => 
      api.post<ApiResponse<Order>>('/orders', order),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      setIsOpen(false);
      reset();
      toast.success('Order created successfully!');
    },
    onError: (error) => {
      console.error('Create order error:', error);
      toast.error(error instanceof Error ? error.message : 'Failed to create order');
    }
  });

  const { register, handleSubmit, reset, control, formState: { errors } } = useForm<OrderCreateForm>();

  const onSubmit = (data: OrderCreateForm) => {
    try {
      createMutation.mutate({
        productId: Number(data.productId),
        customerId: Number(data.customerId),
        quantity: Number(data.quantity)
      });
    } catch (error) {
      console.error('Form submission error:', error);
      toast.error('Failed to process order data');
    }
  };

  const customers = customersData?.data?.content || [];
  const products = productsData?.data?.content || [];

  return (
    <Dialog open={isOpen} onOpenChange={(open) => {
      setIsOpen(open);
      if (!open) reset();
    }}>
      <DialogTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Create Order
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Create New Order</DialogTitle>
          <DialogDescription>
            Create a new order for a customer.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="customerId">Customer</Label>
            <Controller
              name="customerId"
              control={control}
              rules={{ required: 'Customer is required' }}
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a customer" />
                  </SelectTrigger>
                  <SelectContent>
                    {customers.map((customer) => (
                      <SelectItem key={customer.id} value={customer.id!.toString()}>
                        {customer.firstName} {customer.lastName} ({customer.email})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.customerId && (
              <p className="text-sm text-red-500">{errors.customerId.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="productId">Product</Label>
            <Controller
              name="productId"
              control={control}
              rules={{ required: 'Product is required' }}
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a product" />
                  </SelectTrigger>
                  <SelectContent>
                    {products.map((product) => (
                      <SelectItem key={product.id} value={product.id!.toString()}>
                        {product.name} (${product.price?.toFixed(2)}) - Stock: {product.stock}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.productId && (
              <p className="text-sm text-red-500">{errors.productId.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="quantity">Quantity</Label>
            <Input
              id="quantity"
              type="number"
              min="1"
              {...register('quantity', { 
                required: 'Quantity is required',
                min: { value: 1, message: 'Quantity must be at least 1' },
                valueAsNumber: true
              })}
              placeholder="1"
            />
            {errors.quantity && (
              <p className="text-sm text-red-500">{errors.quantity.message}</p>
            )}
          </div>

          <div className="flex justify-end space-x-2">
            <Button 
              type="button" 
              variant="outline" 
              onClick={() => setIsOpen(false)}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Creating...' : 'Create Order'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
