/*
 * Pixy.h
 *
 *  Created on: Jan 5, 2017
 *      Author: rosly_000
 */

#ifndef PIXY_H_
#define PIXY_H_


#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>

// using an SPI interface  
#define SPI 

#define PIXY_ARRAYSIZE              100
#define PIXY_START_WORD             0xaa55
#define PIXY_START_WORD_CC          0xaa56
#define PIXY_START_WORDX            0x55aa
#define PIXY_SERVO_SYNC             0xff
#define PIXY_CAM_BRIGHTNESS_SYNC    0xfe
#define PIXY_LED_SYNC               0xfd
#define PIXY_OUTBUF_SIZE            64

#define PIXY_SYNC_BYTE              0x5a
#define PIXY_SYNC_BYTE_DATA         0x5b

#define ROBORIO_SPI_PORT            0

// the routines
void init();
int getStart(void);
uint16_t getBlocks(uint16_t maxBlocks);
int setServos(uint16_t s0, uint16_t s1);
int setBrightness(uint8_t brightness);
int setLED(uint8_t r, uint8_t g, uint8_t b);

// data types
typedef enum 
{
    NORMAL_BLOCK,
    CC_BLOCK // color code block
} BlockType;

typedef struct  
{
  uint16_t signature; 
  uint16_t x;
  uint16_t y;
  uint16_t width;
  uint16_t height;
  uint16_t angle; // angle is only available for color coded blocks
} Block;

// communication routines
static uint16_t getWord(void);
static int send(uint8_t *data, int len);




#endif /* PIXY_H_ */
