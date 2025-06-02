
import { Edit, ShoppingCart, User, Package } from 'lucide-react';
import { Order } from '@/types/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/hooks/useAuth';

interface OrderCardProps {
  order: Order;
  onEditStatus: (order: Order) => void;
}

export function OrderCard({ order, onEditStatus }: OrderCardProps) {
  const { isAdmin } = useAuth();

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case 'PENDING': return 'outline';
      case 'PROCESSING': return 'default';
      case 'SHIPPED': return 'secondary';
      case 'DELIVERED': return 'default';
      case 'CANCELLED': return 'destructive';
      case 'RETURNED': return 'secondary';
      default: return 'outline';
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Invalid Date';
    }
  };

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="pb-4">
        <div className="flex items-start justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <ShoppingCart className="h-6 w-6 text-primary" />
            </div>
            <div>
              <CardTitle className="text-lg">Order #{order.id}</CardTitle>
              <div className="flex items-center space-x-4 mt-1">
                <Badge variant={getStatusBadgeVariant(order.status)}>
                  {order.status}
                </Badge>
                <span className="text-sm text-muted-foreground">
                  {formatDate(order.orderDate)}
                </span>
              </div>
            </div>
          </div>
          <div className="flex space-x-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onEditStatus(order)}
            >
              <Edit className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex items-center space-x-2">
            <User className="h-4 w-4 text-muted-foreground" />
            <div>
              <p className="text-sm font-medium">Customer</p>
              <p className="text-sm text-muted-foreground">
                {order.customer?.firstName || 'N/A'} {order.customer?.lastName || ''}
              </p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <Package className="h-4 w-4 text-muted-foreground" />
            <div>
              <p className="text-sm font-medium">Product</p>
              <p className="text-sm text-muted-foreground">{order.product?.name || 'N/A'}</p>
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <div>
              <p className="text-sm font-medium">Total Amount</p>
              <p className="text-lg font-semibold text-green-600">
                ${(order.totalAmount || 0).toFixed(2)}
              </p>
              <p className="text-xs text-muted-foreground">
                {order.quantity || 0} × ${(order.unitPrice || 0).toFixed(2)}
              </p>
            </div>
          </div>
        </div>
        {isAdmin && order.createdBy && (
          <div className="text-xs text-muted-foreground border-t pt-2">
            Created by: {order.createdBy.firstName} {order.createdBy.lastName}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
