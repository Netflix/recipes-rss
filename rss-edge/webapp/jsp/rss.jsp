<%
 /*
 * Copyright 2012 Netflix, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
%>
<%@page import="com.netflix.hystrix.HystrixCommand"%>
<%@page import="com.netflix.recipes.rss.hystrix.AddRSSCommand"%>
<%@page import="com.netflix.recipes.rss.hystrix.DeleteRSSCommand"%>

<%@ page import="com.netflix.recipes.rss.hystrix.GetRSSCommand" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.concurrent.Future" %>

<%
    // TODO: Fix the bootstrap css and js accessibility issue
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="refresh">
    <title>Netflix OSS RSS Reader</title>

    <!-- TODO: Should host locally -->
    <link href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }
    </style>
    <link href="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/css/bootstrap.min.css" rel="stylesheet">
</head>

<body>

<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="brand" href="#">Netflix OSS RSS Reader</a>

            <div class="nav-collapse collapse">
                <form class="navbar-form pull-right" method="POST" action="/jsp/rss.jsp">
                    <input class="span8" type="text" placeholder="Enter the feed Url" name="url">
                    <button type="submit" class="btn">Add</button>
                </form>
            </div>
            <!--/.nav-collapse -->
        </div>
    </div>
</div>

<%
    // Delete a RSS feed
    String delFeedUrl = request.getParameter("delFeedUrl");
    if (delFeedUrl != null) {
        HystrixCommand<String> deleteCommand = new DeleteRSSCommand(delFeedUrl);
        Future<String> future = deleteCommand.queue();
        String responseString = future.get();
        response.sendRedirect("/jsp/rss.jsp");
    }

    // Add a RSS feed
    String url = request.getParameter("url");
    if (url != null) {
        url = URLEncoder.encode(url, "UTF-8");
        HystrixCommand<String> addCommand = new AddRSSCommand(url);
        Future<String> future = addCommand.queue();
        String responseString = future.get();
        response.sendRedirect("/jsp/rss.jsp");
    }

    // Get RSS feeds
    HystrixCommand<String> getCommand = new GetRSSCommand();
    Future<String> future = getCommand.queue();
    String responseString = future.get();

    // When a user has only 1 subcription, middle tier returns a jsonobject instead of an array
    final JSONObject jo = new JSONObject(responseString);
    JSONArray subscriptions = new JSONArray();
    if (jo.has("subscriptions")) {
        if (jo.get("subscriptions") instanceof JSONObject) {
            subscriptions.put(jo.get("subscriptions"));
        } else {
            subscriptions = jo.getJSONArray("subscriptions");
        }
    }

    // Compute the number of rows
    int numFeeds  = subscriptions.length();
    int totalRows = (numFeeds % 3 == 0)? (numFeeds/3) : (numFeeds/3) + 1;
%>

<!-- To hide/show the delete button -->
<style type="text/css">
    h4 .icon-remove {
        visibility:hidden;
    }
    h4:hover .icon-remove {
        visibility:visible;
    }
</style>

<div class="container">
    <%
        int index = 0;
        int itemIndex = 0;
        for (int rowIndex = 0; rowIndex < totalRows; rowIndex++) {
    %>

    <div class="row">
        <%
            for (int colIndex = 0; colIndex < 3; colIndex++) {
                if (index >= numFeeds) break;
                JSONObject rss = (JSONObject) subscriptions.get(index);
        %>
        <div class="span4">

            <h4><%=rss.get("title")%> <a href="?delFeedUrl=<%=URLEncoder.encode((String) rss.get("url"), "UTF-8")%>"><i class="icon-remove"></i> </a></h4>
            <%
                JSONArray items = (JSONArray) rss.get("items");
                for (int i = 0; i < items.length(); i++) {
                    // Only 3 feeds per row
                    if (i > 4) break;
                    JSONObject item = (JSONObject) items.get(i);
            %>
            <p id="item<%=itemIndex%>" data-container="body" data-content="<%=((String)item.get("description")).replace('"','\'')%>" data-trigger="hover" data-placement="right" data-html="true"><a href="<%=item.get("link")%>"><%=item.get("title")%></a></p>
            <%
                    itemIndex++;
                }
            %>
        </div>
        <%
                index++;
            }
        %>
    </div>
    <%
        }
    %>

</div>


<hr>
<footer align="center">
    <p>Netflix Inc. 2013</p>
</footer>
</div> <!-- /container -->

<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="http://code.jquery.com/jquery.js"></script>
<script src="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/js/bootstrap.js"></script>
<script src="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/js/bootstrap.min.js"></script>
<script>
    $(function ()
    {
        <%
            for (int i = 0; i <  itemIndex; i++) {
        %>
                $("#item<%=i%>").popover();
        <%
            }
        %>
    });
</script>
</body>
</html>
