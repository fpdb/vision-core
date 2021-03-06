package org.vision.core.services.http;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vision.common.utils.ByteArray;
import org.vision.core.Wallet;
import org.vision.protos.Protocol.MarketOrderList;
import org.vision.protos.Protocol.MarketOrderPair;


@Component
@Slf4j(topic = "API")
public class GetMarketOrderListByPairServlet extends RateLimiterServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      boolean visible = Util.getVisible(request);

      String sellTokenId = request.getParameter("sell_token_id");
      String buyTokenId = request.getParameter("buy_token_id");

      if (visible) {
        sellTokenId = Util.getHexString(sellTokenId);
        buyTokenId = Util.getHexString(buyTokenId);
      }

      MarketOrderList reply = wallet.getMarketOrderListByPair(ByteArray.fromHexString(sellTokenId),
          ByteArray.fromHexString(buyTokenId));
      if (reply != null) {
        response.getWriter().println(JsonFormat.printToString(reply, visible));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      Util.checkBodySize(input);
      boolean visible = Util.getVisiblePost(input);

      MarketOrderPair.Builder build = MarketOrderPair.newBuilder();
      JsonFormat.merge(input, build, visible);

      MarketOrderList reply = wallet.getMarketOrderListByPair(build.getSellTokenId().toByteArray(),
          build.getBuyTokenId().toByteArray());
      if (reply != null) {
        response.getWriter().println(JsonFormat.printToString(reply, visible));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      Util.processError(e, response);
    }
  }
}
