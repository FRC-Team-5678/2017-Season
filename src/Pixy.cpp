#include "Pixy.h"

// SPI sends as it receives so we need a getByte routine that 
// takes an output data argument

uint8_t getByte(uint8_t out)
{
   uint8_t* dataRecieved;
   uint8_t* ptrDataToSend = &out;
   uint8_t bytesTransferred;
   bytesTransferred = SPI::spiTransaction(ROBORIO_SPI_PORT, ptrDataToSend, dataRecieved, 1);
   if (bytesTransferred != 1)
	   return 0;
   else
    return *dataRecieved;
}

// variables for a little circular queue
static uint8_t g_outBuf[PIXY_OUTBUF_SIZE];
static uint8_t g_outLen = 0;
static uint8_t g_outWriteIndex = 0;
static uint8_t g_outReadIndex = 0;


/*
PIXY_ARRAYSIZE              100
PIXY_START_WORD             0xaa55
 PIXY_START_WORD_CC          0xaa56
 PIXY_START_WORDX            0x55aa
 PIXY_SERVO_SYNC             0xff
PIXY_CAM_BRIGHTNESS_SYNC    0xfe
PIXY_LED_SYNC               0xfd
PIXY_OUTBUF_SIZE            64

PIXY_SYNC_BYTE              0x5a  to sync SPI data
PIXY_SYNC_BYTE_DATA         0x5b  to sync/indicate SPI send data

The SPI protocol requires data communication in both directions.  The master (roboRIO) must send data in order
to receive data.  The Pixy sends data in a specific format:  For each image frame, the Pixy sends a block of data
describing each recognized target object.  We can specify the max number of blocks sent per frame, using the "Max blocks" parameter.
  The start of each object block is marked with a sync word.  There are two possible sync word value, and that
  value serves to identify the block as representing a NORMAL object or a COLOR CODE object.   Each object block is formatted
as described in the Pixy wiki http://cmucam.org/projects/cmucam5/wiki/Porting_Guide

All values in the object block are 16-bit words, sent least-signifcant byte first (little endian). So, for example, when sending the sync word 0xaa55, Pixy sends 0x55 (first byte) then 0xaa (second byte).
When there are no detected objects (no data) Pixy sends zeros
Object block format
Bytes    16-bit word    Description
----------------------------------------------------------------
0, 1     y              sync: 0xaa55=normal object, 0xaa56=color code object
2, 3     y              checksum (sum of all 16-bit words 2-6, that is, bytes 4-13)
4, 5     y              signature number
6, 7     y              x center of object
8, 9     y              y center of object
10, 11   y              width of object
12, 13   y              height of object
To mark between frames an extra sync word (0xaa55) is inserted. This means that a new image frame is indicated by either:

Two sync words sent back-to-back (0xaa55, 0xaa55), or
a normal sync followed by a color-code sync (0xaa55, 0xaa56).
So, a typical way to parse the serial stream is to wait for two sync words and then start parsing the object block, using the sync words to indicate the start of the next object block, and so on.

Earlier we mentioned that SPI is simultaneous send/receive interface, so the  roboRIO must send a byte of data to the Pixy for each byte it receives.

The output buffer (array) provides an abstraction, so the roboRIO can "send" multiple bytes into the
buffer (up to the maximum of PIXY_OUTBUF_SIZE), and they will be transmitted as part of the getWord method.

To save CPU, Pixy configures its SPI controller with 16-bit words instead of 8. This works great, but the 16-bit words are sent big-endian instead of little-endian.
Pixy relies on sync bytes sent to it to make sure it has good bit-sync, so you need to send a sync byte every other byte when talking to Pixy over SPI. This also solves the data imbalance problem with Pixy and SPI -- that is, there's a lot more data being sent by Pixy than being received by Pixy. The sync bytes allow Pixy to separate the filler data from the valid data.

So if every other byte sent to the pixy is a sync byte, then the alternate byte which comes
from the buffer can be anything?  Can we remove the output buffer and just send zeros?

initially g_outLen = 0 - this must be the number of bytes currently stored for output
g_outWriteIndex = 0 - points to the next byte to be written in send.
g_outReadIndex = 0 - points to the next byte to be read in getWord.
*/


uint16_t getWord()
{
  // ordering is big endian because Pixy is sending 16 bits through SPI 
  uint16_t w;
  uint8_t c, cout = 0;


  if (g_outLen)  // there is at least one byte stored in the buffer for transmission to the pixy
  {              // the unique sync byte in this case lets the pixy know that real data will follow
    w = getByte(PIXY_SYNC_BYTE_DATA);     //send data sync byte..
    cout = g_outBuf[g_outReadIndex++];    // retrieve value from buffer so it is ready to send at the next transaction
    g_outLen--;
    if (g_outReadIndex==PIXY_OUTBUF_SIZE)
      g_outReadIndex = 0;                 //if the end of the buffer has been reached, reset index to 0
  }
  else
  {   // no data in the output buffer? Send a sync byte along with a zero.
    w = getByte(PIXY_SYNC_BYTE); // send out sync byte
  }
  w <<= 8;                       // on alternate calls, we compose a full word using the just recieved byte along with a byte from the buffer
  c = getByte(cout); // send out data byte.  If there is no data, we send a zero byte
  w |= c;

  return w;
}

int send(uint8_t *data, int len)
{
  int i;

  // check to see if we have enough space in our circular queue
  if (g_outLen+len>PIXY_OUTBUF_SIZE)
    return -1;

  g_outLen += len;
  for (i=0; i<len; i++)
  {
    g_outBuf[g_outWriteIndex++] = data[i];
    if (g_outWriteIndex==PIXY_OUTBUF_SIZE)
      g_outWriteIndex = 0;
  }
  return len;
}

#endif //////////////// end SPI routines

static int g_skipStart = 0;
static BlockType g_blockType;
static Block *g_blocks;

void init()
{
  g_blocks = (Block *)malloc(sizeof(Block)*PIXY_ARRAYSIZE);
}

int getStart(void)
{
  uint16_t w, lastw;

  lastw = 0xffff;

  while(1)
  {
    w = getWord();
    if (w==0 && lastw==0)
      return 0; // no start code.  In I2C and SPI modes this means no data, so return immediately
    else if (w==PIXY_START_WORD && lastw==PIXY_START_WORD)
    {
      g_blockType = NORMAL_BLOCK;
      return 1; // code found!
    }
    else if (w==PIXY_START_WORD_CC && lastw==PIXY_START_WORD)
    {
      g_blockType = CC_BLOCK; // found color code block
      return 1;
    }    
    else if (w==PIXY_START_WORDX) //msb first
#ifdef SPI
      getByte(0); // we're out of sync! (backwards)
#else
      getByte(); // we're out of sync! (backwards)
#endif
    lastw = w; 
  }
}

uint16_t getBlocks(uint16_t maxBlocks)
{
  uint8_t i;
  uint16_t w, blockCount, checksum, sum;
  Block *block;

  if (!g_skipStart)
  {
    if (getStart()==0)
      return 0;
  }
  else
    g_skipStart = 0;

  for(blockCount=0; blockCount<maxBlocks && blockCount<PIXY_ARRAYSIZE;)
  {
    checksum = getWord();
    if (checksum==PIXY_START_WORD) // we've reached the beginning of the next frame
    {
      g_skipStart = 1;
      g_blockType = NORMAL_BLOCK;
      return blockCount;
    }
    else if (checksum==PIXY_START_WORD_CC)
    {
      g_skipStart = 1;
      g_blockType = CC_BLOCK;
      return blockCount;
    }
    else if (checksum==0)
      return blockCount;

    block = g_blocks + blockCount;

    for (i=0, sum=0; i<sizeof(Block)/sizeof(uint16_t); i++)
    {
      if (g_blockType==NORMAL_BLOCK && i>=5) // no angle for normal block
      {
        block->angle = 0;
        break;
      }
      w = getWord();
      sum += w;
      *((uint16_t *)block + i) = w;
    }

    // check checksum
    if (checksum==sum)
      blockCount++;
    else
      printf("checksum error!\n");

    w = getWord();
    if (w==PIXY_START_WORD)
      g_blockType = NORMAL_BLOCK;
    else if (w==PIXY_START_WORD_CC)
      g_blockType = CC_BLOCK;
    else
      return blockCount;
  }
}

int setServos(uint16_t s0, uint16_t s1)
{
  uint8_t outBuf[6];

  outBuf[0] = 0x00;
  outBuf[1] = PIXY_SERVO_SYNC; 
  *(uint16_t *)(outBuf + 2) = s0;
  *(uint16_t *)(outBuf + 4) = s1;

  return send(outBuf, 6);
}

int setBrightness(uint8_t brightness)
{
  uint8_t outBuf[3];

  outBuf[0] = 0x00;
  outBuf[1] = PIXY_CAM_BRIGHTNESS_SYNC; 
  outBuf[2] = brightness;

  return send(outBuf, 3);
}

int setLED(uint8_t r, uint8_t g, uint8_t b)
{
  uint8_t outBuf[5];

  outBuf[0] = 0x00;
  outBuf[1] = PIXY_LED_SYNC; 
  outBuf[2] = r;
  outBuf[3] = g;
  outBuf[4] = b;

  return send(outBuf, 5);
}

