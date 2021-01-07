/*******************************************************************************
 * Copyright (C) 2021 Frédéric Bard
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *******************************************************************************/
package net.tourbook.cloud.oauth2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import net.tourbook.cloud.Messages;
import net.tourbook.common.UI;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public abstract class TokensRetrievalHandler implements HttpHandler {

   protected TokensRetrievalHandler() {}

   @Override
   public void handle(final HttpExchange httpExchange) throws IOException {

      Tokens tokens = null;
      if ("GET".equals(httpExchange.getRequestMethod())) { //$NON-NLS-1$

         tokens = handleGetRequest(httpExchange);
      }

      handleResponse(httpExchange);

      saveTokensInPreferences(tokens);
   }

   private Tokens handleGetRequest(final HttpExchange httpExchange) {

      final char[] separators = { '#', '&', '?' };

      final String response = httpExchange.getRequestURI().toString();

      String authorizationCode = UI.EMPTY_STRING;
      final List<NameValuePair> params = URLEncodedUtils.parse(response, StandardCharsets.UTF_8, separators);
      final Optional<NameValuePair> result = params
            .stream()
            .filter(param -> param.getName().equals(OAuth2Constants.PARAM_CODE)).findAny();

      if (result.isPresent()) {
         authorizationCode = result.get().getValue();
      }

      return retrieveTokens(authorizationCode);
   }

   private void handleResponse(final HttpExchange httpExchange) throws IOException {

      final OutputStream outputStream = httpExchange.getResponseBody();

      final StringBuilder htmlBuilder = new StringBuilder();
      htmlBuilder.append("<html><body><h1>" + Messages.Html_CloseBrowser_Text + "</h1></body></html>"); //$NON-NLS-1$ //$NON-NLS-2$

      // this line is a must
      httpExchange.sendResponseHeaders(200, htmlBuilder.length());

      try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
         writer.write(htmlBuilder.toString());
         outputStream.flush();
      }
   }

   public abstract Tokens retrieveTokens(final String authorizationCode);

   public abstract void saveTokensInPreferences(final Tokens tokens);
}
