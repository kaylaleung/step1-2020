package com.google.step.snippet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.step.snippet.data.Card;
import com.google.step.snippet.external.W3SchoolClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class W3SchoolClientTest {

  private final W3SchoolClient client = new W3SchoolClient();

  @Test
  public void htmlImage() {
    Card actual = client.search("https://www.w3schools.com/tags/tag_img.asp");
    Card expected =
        new Card(
            "HTML <img> Tag",
            "<img src=\"img_girl.jpg\" alt=\"Girl in a jacket\" width=\"500\" height=\"600\">",
            "https://www.w3schools.com/tags/tag_img.asp",
            "How to insert an image:");
    assertEquals(actual, expected);
  }

  @Test
  public void jsJson() {
    Card actual = client.search("https://www.w3schools.com/js/js_json_intro.asp");
    Card expected =
        new Card(
            "JSON - Introduction",
            "var myObj = {name: \"John\", age: 31, city: \"New York\"}; var myJSON ="
                + " JSON.stringify(myObj); window.location = \"demo_json.php?x=\" + myJSON;",
            "https://www.w3schools.com/js/js_json_intro.asp",
            "JSON: JavaScript Object Notation.");
    assertEquals(actual, expected);
  }

  @Test
  public void incompleteCard() {
    Card actual = client.search("https://www.w3schools.com/html/html_quiz.asp");
    assertNull(actual);
  }

  @Test
  public void invalidLink() {
    Card actual = client.search("https://www.w3schools.com/css/html.asp");
    assertNull(actual);
  }

  @Test
  public void blankPage() {
    Card actual = client.search("https://www.w3schools.com/");
    assertNull(actual);
  }
}
