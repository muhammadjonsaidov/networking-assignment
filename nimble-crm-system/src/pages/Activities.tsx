
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, Activity as ActivityIcon, User, Calendar, Filter } from 'lucide-react';
import { api } from '@/lib/api';
import { Activity, ApiResponse, Page } from '@/types/api';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

export default function Activities() {
  const [searchTerm, setSearchTerm] = useState('');
  const [actionFilter, setActionFilter] = useState<string>('all');

  const { data: activitiesData, isLoading } = useQuery({
    queryKey: ['activities'],
    queryFn: () => api.get<ApiResponse<Page<Activity>>>('/activities')
  });

  const activities = activitiesData?.data?.content || [];
  
  const filteredActivities = activities.filter(activity => {
    const matchesSearch = 
      activity.actor.toLowerCase().includes(searchTerm.toLowerCase()) ||
      activity.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
      activity.details.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesFilter = actionFilter === 'all' || activity.action.includes(actionFilter.toUpperCase());
    
    return matchesSearch && matchesFilter;
  });

  const getActivityBadgeColor = (action: string) => {
    if (action.includes('CREATED')) return 'default';
    if (action.includes('UPDATED')) return 'secondary';
    if (action.includes('DELETED')) return 'destructive';
    if (action.includes('LOGIN')) return 'outline';
    if (action.includes('PASSWORD')) return 'secondary';
    if (action.includes('TOKEN')) return 'outline';
    return 'outline';
  };

  const getActivityIcon = (action: string) => {
    if (action.includes('USER')) return <User className="h-4 w-4" />;
    if (action.includes('CUSTOMER')) return <User className="h-4 w-4" />;
    if (action.includes('PRODUCT')) return <ActivityIcon className="h-4 w-4" />;
    if (action.includes('ORDER')) return <ActivityIcon className="h-4 w-4" />;
    if (action.includes('LOGIN') || action.includes('TOKEN')) return <ActivityIcon className="h-4 w-4" />;
    return <ActivityIcon className="h-4 w-4" />;
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    return {
      date: date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      }),
      time: date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
      })
    };
  };

  const getUniqueActions = () => {
    const actions = new Set<string>();
    activities.forEach(activity => {
      const actionType = activity.action.split('_')[0];
      actions.add(actionType);
    });
    return Array.from(actions);
  };

  const groupActivitiesByDate = (activities: Activity[]) => {
    const grouped: { [key: string]: Activity[] } = {};
    
    activities.forEach(activity => {
      const dateKey = formatTimestamp(activity.timestamp).date;
      if (!grouped[dateKey]) {
        grouped[dateKey] = [];
      }
      grouped[dateKey].push(activity);
    });
    
    return grouped;
  };

  const groupedActivities = groupActivitiesByDate(filteredActivities);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Activities</h1>
          <p className="text-muted-foreground">
            Track all system activities and user actions.
          </p>
        </div>
      </div>

      <div className="flex items-center space-x-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="Search activities..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex items-center space-x-2">
          <Filter className="h-4 w-4 text-muted-foreground" />
          <Select value={actionFilter} onValueChange={setActionFilter}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Filter by action" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Actions</SelectItem>
              {getUniqueActions().map(action => (
                <SelectItem key={action} value={action.toLowerCase()}>
                  {action.charAt(0) + action.slice(1).toLowerCase()}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-8">
          <div>Loading activities...</div>
        </div>
      ) : (
        <div className="space-y-6">
          {Object.keys(groupedActivities)
            .sort((a, b) => new Date(b).getTime() - new Date(a).getTime())
            .map(dateKey => (
            <div key={dateKey} className="space-y-4">
              <div className="flex items-center space-x-2">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <h3 className="text-lg font-semibold">{dateKey}</h3>
                <Badge variant="outline">
                  {groupedActivities[dateKey].length} activities
                </Badge>
              </div>
              
              <div className="space-y-3">
                {groupedActivities[dateKey]
                  .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
                  .map((activity) => {
                    const { time } = formatTimestamp(activity.timestamp);
                    return (
                      <Card key={activity.id} className="hover:shadow-sm transition-shadow">
                        <CardContent className="p-4">
                          <div className="flex items-start space-x-4">
                            <div className="flex-shrink-0 p-2 bg-primary/10 rounded-lg">
                              {getActivityIcon(activity.action)}
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center space-x-2 mb-2">
                                <Badge 
                                  variant={getActivityBadgeColor(activity.action)}
                                  className="text-xs"
                                >
                                  {activity.action.replace(/_/g, ' ')}
                                </Badge>
                                <span className="text-sm text-muted-foreground">{time}</span>
                              </div>
                              <div className="flex items-center space-x-2 mb-1">
                                <span className="font-medium text-sm">{activity.actor}</span>
                                <span className="text-muted-foreground">•</span>
                                <span className="text-sm text-muted-foreground">
                                  {activity.details}
                                </span>
                              </div>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
              </div>
            </div>
          ))}
          
          {filteredActivities.length === 0 && (
            <Card>
              <CardContent className="text-center py-8">
                <ActivityIcon className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No activities found</h3>
                <p className="text-muted-foreground">
                  {searchTerm || actionFilter !== 'all' 
                    ? 'Try adjusting your search criteria.'
                    : 'Activities will appear here as users interact with the system.'}
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      )}
    </div>
  );
}
