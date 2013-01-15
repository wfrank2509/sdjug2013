//
//  MasterViewController.m
//  SensorData
//
//  Created by Wolfgang Frank on 14.01.13.
//  Copyright (c) 2013 arconsis IT-Solutions GmbH. All rights reserved.
//

#import "MasterViewController.h"


@interface MasterViewController ()

@property (nonatomic, strong) NSArray *jsonResultsArray;
@property (nonatomic, strong) NSOperationQueue *queue;
@end

@implementation MasterViewController

- (void)awakeFromNib
{
    [super awakeFromNib];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    UIRefreshControl *refreshControl = [[UIRefreshControl alloc]
                                        init];
    [refreshControl addTarget:self
                       action:@selector(refreshData)
             forControlEvents:UIControlEventValueChanged];
    self.refreshControl = refreshControl;

    self.jsonResultsArray = [NSArray array];
    self.queue = [[NSOperationQueue alloc] init];
    
    [self refreshData];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Table View

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.jsonResultsArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell" forIndexPath:indexPath];

    NSDictionary *object = self.jsonResultsArray[indexPath.row];
    cell.textLabel.text = [NSString stringWithFormat:@"SensorData: %@", [object objectForKey:@"SensorData"]];

    long long timestamp = [((NSNumber*)[object objectForKey:@"created"]) longLongValue];
    // in the API, the time interval is in seconds, not milliseconds
    NSTimeInterval timeInterval = (double)(timestamp/1000);

    NSDate *theDate = [[NSDate alloc]initWithTimeIntervalSince1970: timeInterval];
    NSDateFormatter* dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setTimeZone:[NSTimeZone timeZoneWithName:@"PST"]];
    [dateFormat setDateFormat:@"yyyy.MM.dd 'at' HH:mm:ss zzz"];
    NSString* theDateString = [dateFormat stringFromDate:theDate];
    cell.detailTextLabel.text = [NSString stringWithFormat:@"Created: %@", theDateString];

    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}


- (void)setNetworkActivityIndicatorVisible:(BOOL)setVisible {
    static NSInteger numberOfCallsToSetVisible = 0;
    if (setVisible)
        numberOfCallsToSetVisible++;
    else
        numberOfCallsToSetVisible--;

    // The assertion helps to find programmer errors in activity indicator management.
    // Since a negative NumberOfCallsToSetVisible is not a fatal error,
    // it should probably be removed from production code.
    NSAssert(numberOfCallsToSetVisible >= 0, @"Network Activity Indicator was asked to hide more often than shown");

    // Display the indicator as long as our static counter is > 0.
    [[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:(numberOfCallsToSetVisible > 0)];
}


- (void)refreshData
{
    [self setNetworkActivityIndicatorVisible:YES];
    NSURL *url = [NSURL URLWithString:@"http://smarties.local:8090/datastore/sensordata"];

    NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:url];
    [urlRequest setHTTPMethod:@"GET"];

    [NSURLConnection sendAsynchronousRequest:urlRequest
                                       queue:self.queue
                           completionHandler:^(NSURLResponse *response,
                                               NSData *data,
                                               NSError *error)
     {
         if ([data length] >0 && error == nil)
         {
             NSDictionary *jsonResponse = [NSJSONSerialization JSONObjectWithData:data
                                                                          options:NSJSONReadingMutableContainers
                                                                            error:nil];
             self.jsonResultsArray = [jsonResponse valueForKey:@"results"];
         }
         else if ([data length] == 0 && error == nil)
         {
             NSLog(@"Nothing was downloaded.");
         }
         else if (error != nil){
             NSLog(@"Error = %@", error);
         }
         dispatch_async(dispatch_get_main_queue(), ^{
             [self setNetworkActivityIndicatorVisible:NO];
             [self.refreshControl endRefreshing];
             [self.tableView reloadData];
         });
     }];

}
@end
