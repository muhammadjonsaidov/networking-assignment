
import { useQuery } from '@tanstack/react-query';
import { Calendar, Users, Activity as ActivityIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { ApiResponse, Customer, Activity } from '@/types/api';
import { DashboardStats } from '@/components/dashboard/DashboardStats';
import { SalesChart } from '@/components/dashboard/SalesChart';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';

export default function Dashboard() {
  const { data: recentCustomers } = useQuery({
    queryKey: ['recent-customers'],
    queryFn: () => api.get<ApiResponse<Customer[]>>('/dashboard/recent-customers?count=5')
  });

  const { data: recentActivities } = useQuery({
    queryKey: ['recent-activities'],
    queryFn: () => api.get<ApiResponse<Activity[]>>('/dashboard/recent-activities?count=10')
  });

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getInitials = (firstName: string, lastName: string) => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase();
  };

  const getActivityBadgeColor = (action: string) => {
    if (action.includes('CREATED')) return 'bg-green-100 text-green-800';
    if (action.includes('UPDATED')) return 'bg-blue-100 text-blue-800';
    if (action.includes('DELETED')) return 'bg-red-100 text-red-800';
    if (action.includes('LOGIN')) return 'bg-purple-100 text-purple-800';
    return 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">
          Welcome back! Here's what's happening with your business today.
        </p>
      </div>

      {/* Stats Cards */}
      <DashboardStats />

      {/* Charts and Recent Data */}
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Sales Chart - Takes 2 columns */}
        <div className="lg:col-span-2">
          <SalesChart />
        </div>

        {/* Recent Customers */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center">
              <Users className="mr-2 h-5 w-5" />
              Recent Customers
            </CardTitle>
            <CardDescription>
              Latest customer registrations
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {recentCustomers?.data?.map((customer) => (
              <div key={customer.id} className="flex items-center space-x-3">
                <Avatar>
                  <AvatarFallback>
                    {getInitials(customer.firstName, customer.lastName)}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium">
                    {customer.firstName} {customer.lastName}
                  </p>
                  <p className="text-sm text-muted-foreground truncate">
                    {customer.email}
                  </p>
                </div>
                <div className="text-xs text-muted-foreground">
                  {formatDate(customer.createdAt!)}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      {/* Recent Activities */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <ActivityIcon className="mr-2 h-5 w-5" />
            Recent Activities
          </CardTitle>
          <CardDescription>
            Latest system activities and user actions
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {recentActivities?.data?.map((activity) => (
              <div key={activity.id} className="flex items-start space-x-3">
                <div className="flex-shrink-0">
                  <Badge className={getActivityBadgeColor(activity.action)}>
                    {activity.action.replace(/_/g, ' ')}
                  </Badge>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm text-muted-foreground">
                    <span className="font-medium text-foreground">{activity.actor}</span>
                    {' • '}
                    {activity.details}
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {formatDate(activity.timestamp)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
