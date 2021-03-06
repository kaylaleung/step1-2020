<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>User Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="css/user_style.css">
    <div class="heading">
      <a class="logo" href="/">
        <h1>
          <span class="blue">S</span><span class="red">n</span><span class="yellow">i</span><span
          class="blue">p</span><span class="green">p</span><span class="red">e</span><span
          class="yellow">t</span>
        </h1>
      </a>
    </div>
  </head>
  <body>
    <a class="home-button" href="/">Home</a>
    <form class="setting" action="/user" method="POST">
      <div class="row">
        <label for="input-websites">Choose your preferred website:</label>
        <select class="input-websites" id="input-websites" name="website">
          <c:choose>
            <c:when test="${not empty user.getWebsite()}">
              <option value="${user.getWebsite()}" selected hidden>${user.getWebsite()}</option>
            </c:when>
            <c:otherwise>
              <option value="" selected disabled hidden>Select</option>
            </c:otherwise>
          </c:choose>
          <option value="GeeksForGeeks">GeeksForGeeks</option>
          <option value="StackOverflow">StackOverflow</option>
          <option value="W3Schools">W3Schools</option>
        </select>
      </div>
      <div class="row">
        <label for="input-languages">Choose your primary coding language:</label>
        <input class="input-languages" id="input-languages" type="text" name="language"
        <c:if test="${not empty user.getLanguage()}"> value="${user.getLanguage()}"</c:if>
               placeholder="Input a coding language">
      </div>
      <input class="submit-button" type="submit" value="Submit">
    </form>
  </body>
</html>
