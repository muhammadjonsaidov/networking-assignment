
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search } from 'lucide-react';
import { api } from '@/lib/api';
import { Order, ApiResponse, Page } from '@/types/api';
import { Input } from '@/components/ui/input';
import { useAuth } from '@/hooks/useAuth';
import { CreateOrderDialog } from '@/components/orders/CreateOrderDialog';
import { OrderCard } from '@/components/orders/OrderCard';
import { OrderStatusDialog } from '@/components/orders/OrderStatusDialog';

export default function Orders() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const { isAdmin } = useAuth();

  const { data: ordersData, isLoading, error } = useQuery({
    queryKey: ['orders'],
    queryFn: () => {
      try {
        return isAdmin ? 
          api.get<ApiResponse<Page<Order>>>('/orders') : 
          api.get<ApiResponse<Page<Order>>>('/orders/my-orders');
      } catch (error) {
        console.error('Error fetching orders:', error);
        throw error;
      }
    }
  });

  const openStatusDialog = (order: Order) => {
    setSelectedOrder(order);
    setIsStatusDialogOpen(true);
  };

  const closeStatusDialog = () => {
    setIsStatusDialogOpen(false);
    setSelectedOrder(null);
  };

  const orders = ordersData?.data?.content || [];
  
  const filteredOrders = orders.filter(order => {
    if (!order) return false;
    
    const searchLower = searchTerm.toLowerCase();
    return (
      (order.customer?.firstName?.toLowerCase().includes(searchLower) || false) ||
      (order.customer?.lastName?.toLowerCase().includes(searchLower) || false) ||
      (order.product?.name?.toLowerCase().includes(searchLower) || false) ||
      (order.status?.toLowerCase().includes(searchLower) || false) ||
      (order.id?.toString().includes(searchTerm) || false)
    );
  });

  if (error) {
    console.error('Orders page error:', error);
    return (
      <div className="space-y-6">
        <div className="text-center py-8">
          <p className="text-red-500">Error loading orders. Please try again.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Orders</h1>
          <p className="text-muted-foreground">
            {isAdmin ? 'Manage all customer orders' : 'View your order history'} and track their status.
          </p>
        </div>
        <CreateOrderDialog />
      </div>

      <div className="flex items-center space-x-2">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="Search orders..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
      </div>

      {isLoading ? (
        <div className="text-center py-8">
          <p>Loading orders...</p>
        </div>
      ) : filteredOrders.length === 0 ? (
        <div className="text-center py-8">
          <p className="text-muted-foreground">
            {searchTerm ? 'No orders found matching your search.' : 'No orders found.'}
          </p>
        </div>
      ) : (
        <div className="grid gap-6">
          {filteredOrders.map((order) => (
            <OrderCard
              key={order.id}
              order={order}
              onEditStatus={openStatusDialog}
            />
          ))}
        </div>
      )}

      <OrderStatusDialog
        order={selectedOrder}
        isOpen={isStatusDialogOpen}
        onClose={closeStatusDialog}
      />
    </div>
  );
}
