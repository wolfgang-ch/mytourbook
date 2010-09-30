////////////////////////////////////////////////////////////////////////////////
// The following FIT Protocol software provided may be used with FIT protocol
// devices only and remains the copyrighted property of Dynastream Innovations Inc.
// The software is being provided on an "as-is" basis and as an accommodation,
// and therefore all warranties, representations, or guarantees of any kind
// (whether express, implied or statutory) including, without limitation,
// warranties of merchantability, non-infringement, or fitness for a particular
// purpose, are specifically disclaimed.
//
// Copyright 2008 Dynastream Innovations Inc.
////////////////////////////////////////////////////////////////////////////////
// ****WARNING****  This file is auto-generated!  Do NOT edit this file.
// Profile Version = 1.0Release
// Tag = $Name: AKW1_000 $
////////////////////////////////////////////////////////////////////////////////


package com.garmin.fit;


public class HrZoneMesg extends Mesg {


   public HrZoneMesg() {
      super(Factory.createMesg(MesgNum.HR_ZONE));
   }

   public HrZoneMesg(final Mesg mesg) {
      super(mesg);
   }
   
   /**
    * Get message_index field
    *
    * @return message_index
    */
   public Integer getMessageIndex() {
      return getFieldIntegerValue(254);
   }

   /**
    * Set message_index field
    *
    * @param messageIndex
    */
   public void setMessageIndex(Integer messageIndex) {
      setFieldValue("message_index", messageIndex);
   }   
   /**
    * Get high_bpm field
    * Units: bpm
    *
    * @return high_bpm
    */
   public Short getHighBpm() {
      return getFieldShortValue(1);
   }

   /**
    * Set high_bpm field
    * Units: bpm
    *
    * @param highBpm
    */
   public void setHighBpm(Short highBpm) {
      setFieldValue("high_bpm", highBpm);
   }   
   /**
    * Get name field
    *
    * @return name
    */
   public String getName() {
      return getFieldStringValue(2);
   }

   /**
    * Set name field
    *
    * @param name
    */
   public void setName(String name) {
      setFieldValue("name", name);
   }

}
