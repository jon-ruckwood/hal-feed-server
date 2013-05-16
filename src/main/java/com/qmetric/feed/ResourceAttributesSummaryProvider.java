package com.qmetric.feed;

import java.util.Map;

public interface ResourceAttributesSummaryProvider
{
    Map<String, String> filterAttributesForSummary(Resource resource);
}
