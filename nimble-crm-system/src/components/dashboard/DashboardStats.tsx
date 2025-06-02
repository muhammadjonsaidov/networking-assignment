
import { useQuery } from '@tanstack/react-query';
import { TrendingUp, TrendingDown, Package, DollarSign, ShoppingCart, BarChart3 } from 'lucide-react';
import { api } from '@/lib/api';
import { DashboardStats as Stats, ApiResponse } from '@/types/api';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';

export function DashboardStats() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: () => api.get<ApiResponse<Stats>>('/dashboard/stats')
  });

  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <Skeleton className="h-4 w-20" />
              <Skeleton className="h-4 w-4" />
            </CardHeader>
            <CardContent>
              <Skeleton className="h-8 w-24 mb-2" />
              <Skeleton className="h-4 w-32" />
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  const statsData = stats?.data;
  if (!statsData) return null;

  const formatCurrency = (value: number) => 
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);

  const formatPercentage = (value: number) => `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`;

  const statCards = [
    {
      title: 'Total Products',
      value: statsData.totalProduct,
      icon: Package,
      color: 'text-blue-600',
    },
    {
      title: 'Revenue',
      value: formatCurrency(statsData.productRevenue),
      change: statsData.revenueChange,
      icon: DollarSign,
      color: 'text-green-600',
    },
    {
      title: 'Products Sold',
      value: statsData.productSold,
      change: statsData.soldChange,
      icon: ShoppingCart,
      color: 'text-purple-600',
    },
    {
      title: 'Avg Monthly Sales',
      value: statsData.avgMonthlySales.toFixed(1),
      change: statsData.avgSalesChange,
      icon: BarChart3,
      color: 'text-orange-600',
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {statCards.map((stat, index) => (
        <Card key={index}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
            <stat.icon className={`h-4 w-4 ${stat.color}`} />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stat.value}</div>
            {stat.change !== undefined && (
              <p className="text-xs text-muted-foreground flex items-center mt-1">
                {stat.change >= 0 ? (
                  <TrendingUp className="h-3 w-3 mr-1 text-green-500" />
                ) : (
                  <TrendingDown className="h-3 w-3 mr-1 text-red-500" />
                )}
                <span className={stat.change >= 0 ? 'text-green-600' : 'text-red-600'}>
                  {formatPercentage(stat.change)}
                </span>
                <span className="ml-1">from last month</span>
              </p>
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
