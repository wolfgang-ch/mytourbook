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


#include "fit_field_definition.hpp"

namespace fit
{

FieldDefinition::FieldDefinition(const Field& field)
{
   num = field.GetNum();
   size = field.GetSize();
   type = field.GetType();      
}

FieldDefinition::FieldDefinition(const Field* field)
{
   num = field->GetNum();
   size = field->GetSize();
   type = field->GetType();      
}

FIT_BOOL FieldDefinition::operator==(const FieldDefinition& field)
{
   if (num != field.num)
      return FIT_FALSE;

   if (size != field.size)
      return FIT_FALSE;

   if (type != field.type)
      return FIT_FALSE;

   return FIT_TRUE;
}      

FIT_BOOL FieldDefinition::operator!=(const FieldDefinition& field)
{
   return !(*this==field);
}

FIT_UINT8 FieldDefinition::Write(ostream &file)
{
   file.put(num);
   file.put(size);
   file.put(type);

   return 3;
}

} // namespace fit
