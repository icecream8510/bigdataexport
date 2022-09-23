package com.boco.framework.export.util;

import java.util.Collection;

public class CollectionUtils extends org.apache.commons.collections.CollectionUtils
{
  public static boolean isEmpty(Collection collection)
  {
    return (collection == null) || (collection.size() == 0);
  }
}