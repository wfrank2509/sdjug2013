#define ON(LED) digitalWrite(LED, HIGH);
#define OFF(LED) digitalWrite(LED, LOW);

/* Define our output ports */
const int GREEN  = 5;
const int YELLOW = 6;
const int RED    = 7;

/* THRESHOLD for Wind Category 0 = STORM / 100 = No Wind */
const int T_STORM  = 60;
const int T_WINDY  = 90;

const int flexSensorPin = A0; //analog pin 0

int aw_value = 0;

#define SAMPLE_SIZE 500
int mp;
int hist [SAMPLE_SIZE];

#define REQ_COUNTER 5000
int requestCount = 1;

// the setup routine runs once when you press reset:
void setup()
{ 
  Serial.begin(9600);
  
  for (int i = 0; i < SAMPLE_SIZE; i++) hist[i] = 0;
  
  // initialize the digital pin as an output.
  pinMode(GREEN, OUTPUT);     
  pinMode(YELLOW, OUTPUT); 
  pinMode(RED, OUTPUT);
  
}

// the loop routine runs over and over again forever:
void loop() {
  
  int measured = analogRead(flexSensorPin);
  
  // Serial.print("measured: ");
  // Serial.println(measured);

  // Let us sample some data in a ring buffer and even out fluctations
  mp = (mp % SAMPLE_SIZE);
  hist[mp++] = measured;

  // Serial.println(measured);
  // We need the average to even out the measured sample data
  aw_value = map(average(hist), 385, 525, 0, 100);
  
  setLedStatus(aw_value);
  
  // Send the request to the Axeda gateway evey 1000 sample
  requestCount = requestCount % REQ_COUNTER;
  if (requestCount == 0)
  {
    // showLedSendStatus();
    //Serial.print("aw_value: ");    
    Serial.println(aw_value);
    
  }
  requestCount++;  
  
  //delay(100);               // wait for a second
}


int average(int array[])
{
  int i;
  int n = sizeof(array);
  int sum = 0;

  for(i = 0; i < n; sum += array[ i++ ] );

  //Serial.println(sum / n);

  return sum / n;
}

void showLedSendStatus()
{
   ON(GREEN);
   ON(YELLOW);
   ON(RED);
}

void setLedStatus(int measured)
{
  if (measured > T_WINDY)
  {
    ON(GREEN);
    OFF(YELLOW);
    OFF(RED);
  }
  else if ((measured > T_STORM) && (measured <= T_WINDY))
  {
    OFF(GREEN);
    ON(YELLOW);
    OFF(RED);
  }
  else if (measured <= T_STORM)
  {
    OFF(GREEN);
    OFF(YELLOW);
    ON(RED);
  }
}
