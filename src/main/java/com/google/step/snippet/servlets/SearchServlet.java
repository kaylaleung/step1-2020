// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.step.snippet.servlets;

import com.google.step.snippet.data.Card;
import com.google.step.snippet.external.GeeksForGeeksClient;
import com.google.step.snippet.external.StackOverflowClient;
import com.google.step.snippet.external.W3SchoolClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/** Servlet that handles searches. */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {

  private static final String W3_CSE_ID = "INSERT_W3SCHOOL_CSE_ID";
  private static final String STACK_CSE_ID = "INSERT_STACKOVERFLOW_CSE_ID";
  private static final String GEEKS_CSE_ID = "INSERT_GEEKSFORGEEKS_CSE_ID";
  private static final String API_KEY = "INSERT_API_KEY";
  private static final String CSE_URL = "https://www.googleapis.com/customsearch/v1";
  private static final String CSE_ITEMS = "items";
  private static final String CSE_LINK = "link";
  private static final String CARD_LIST_LABEL = "cardList";

  private static String encodeValue(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      return null;
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws IOException, ServletException {
    String param = request.getParameter("q");
    if (param == null || encodeValue(param) == null) {
      response.setContentType("text/html;");
      response.getWriter().println("Invalid Query");
      return;
    }

    String query = encodeValue(param);
    List<Card> allCards = new ArrayList<>();

    ExecutorService executor = Executors.newFixedThreadPool(3);
    List<Callable<Card>> cardCallbacks = 
      Arrays.asList(
          () -> {
            String w3Link = getLink(W3_CSE_ID, query);
            if (w3Link != null) {
              W3SchoolClient w3client = new W3SchoolClient();
              return w3client.search(w3Link);
            }
            return null;
          },
          ()  -> {
            String stackLink = getLink(STACK_CSE_ID, query);
            if (stackLink != null) {
              StackOverflowClient stackClient = new StackOverflowClient();
              return stackClient.search(stackLink);
            }
            return null;
          },
          () -> {
            String geeksLink = getLink(GEEKS_CSE_ID, query);
            if (geeksLink != null) {
              GeeksForGeeksClient geeksClient = new GeeksForGeeksClient();
              return geeksClient.search(geeksLink);
            }
            return null;
          });
    try {
      executor.invokeAll(cardCallbacks).stream()
          .map(
              future -> {
                try {
                  return future.get();
                } catch (Exception e) {
                  throw new IllegalStateException(e);
                }
              })
          .forEach(
              (c) -> {
                if (c != null) {
                  allCards.add(c);
                }
              });
    } catch (InterruptedException | NullPointerException | RejectedExecutionException e) {
      return;
    }

    request.setAttribute(CARD_LIST_LABEL, allCards);
    request.getRequestDispatcher("WEB-INF/templates/search.jsp").forward(request, response);
  }

  private String getLink(String id, String query) {
    String url = CSE_URL + "?key=" + API_KEY + "&cx=" + id + "&q=" + query;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      try (CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
        if (response.getStatusLine().getStatusCode() != 200) {
          return null;
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
          return null;
        }
        JsonObject obj;
        try {
          obj =
              JsonParser.parseReader(new InputStreamReader(entity.getContent())).getAsJsonObject();
        } catch (JsonParseException
            | IllegalStateException
            | UnsupportedOperationException
            | IOException e) {
          return null;
        }
        if (obj.has(CSE_ITEMS) && obj.getAsJsonArray(CSE_ITEMS).size() > 0) {
          JsonObject topResult = obj.getAsJsonArray(CSE_ITEMS).get(0).getAsJsonObject();
          if (topResult.has(CSE_LINK)) {
            return topResult.get(CSE_LINK).getAsString();
          }
        }
      } catch (IOException | IllegalStateException | ClassCastException e) {
        return null;
      }
    } catch (IOException e) {
      return null;
    }
    return null;
  }
}
