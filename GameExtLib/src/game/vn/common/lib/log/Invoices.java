/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.vn.common.lib.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Thông tin lịch sử ván chơi
 * @author tuanp
 */
public class Invoices implements Cloneable{
    // Thời gian tạo
    private long createdAt;
    // Thời gian kết thúc ván
    private long finishedAt;    // hanv rename for insight
    //tự động bên server tạo unit
    private String requestId;
    //id con game (Z88)
    private int connectionId;
    //id small game in z88
    private int serviceId;
    //Tự động bên server tạo unit
    private String invoiceId;
    //server id
    private int serverId;
    //thông tin khi có phát sinh tiền trong game(su dung cho history của client)
    private List<InvoiceDetail> invoiceDetail = new ArrayList<>();
    //thông tin chi tiết toàn ván game (Sử dụng cho admin)
    private List<BoardDetail> boardDetail = new ArrayList<>();
    //danh sách id database của user
    private List<String> playerIds = new ArrayList<>();;
    //thông tin chi tiết user
    private List<PlayersDetail> playersDetail = new ArrayList<>();
    //thông tin bàn
    private InvoiceInfo invoiceInfo;
    private String merchantId;

    public void reset(){
        createdAt = 0;
        requestId = "";
        invoiceId="";
        invoiceDetail.clear();
        boardDetail.clear();
        playerIds.clear();
        playersDetail.clear();
        invoiceInfo = null;
    }

    public Invoices() {
    }

    public Invoices(int connectionId, int serviceId, int serverId, String merchantId) {
        this.connectionId = connectionId;
        this.serviceId = serviceId;
        this.serverId = serverId;
        this.merchantId = merchantId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    public List<PlayersDetail> getPlayersDetail() {
        return playersDetail;
    }

    public void setPlayersDetail(List<PlayersDetail> playersDetail) {
        this.playersDetail = playersDetail;
    }
    
    public void addPlayersDetail(PlayersDetail playersDetail) {
        this.playersDetail.add(playersDetail);
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public List<InvoiceDetail> getInvoiceDetail() {
        return invoiceDetail;
    }

    public void setInvoiceDetail(List<InvoiceDetail> invoiceDetail) {
        this.invoiceDetail = invoiceDetail;
    }
    
    public void addInvoiceDetail(InvoiceDetail invoiceDetail) {
        this.invoiceDetail.add(invoiceDetail);
    }

    public List<BoardDetail> getBoardDetail() {
        return boardDetail;
    }

    public void setBoardDetail(List<BoardDetail> boardDetail) {
        this.boardDetail = boardDetail;
    }
    public void addBoardDetail(BoardDetail boardDetail) {
        this.boardDetail.add(boardDetail) ;
    }

    public InvoiceInfo getInvoiceInfo() {
        return invoiceInfo;
    }

    public void setInvoiceInfo(InvoiceInfo invoiceInfo) {
        this.invoiceInfo = invoiceInfo;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(long endAt) {
        this.finishedAt = endAt;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public Invoices clone(){
        Invoices invoices = new Invoices();
        invoices.setBoardDetail(new ArrayList<>(boardDetail));
        invoices.setConnectionId(connectionId);
        invoices.setCreatedAt(createdAt);
        invoices.setFinishedAt(finishedAt);
        invoices.setInvoiceDetail(new ArrayList<>(invoiceDetail));
        invoices.setInvoiceId(invoiceId);
        invoices.setInvoiceInfo(invoiceInfo);
        invoices.setPlayerIds(new ArrayList<>(playerIds));
        invoices.setPlayersDetail(new ArrayList<>(playersDetail));
        invoices.setRequestId(requestId);
        invoices.setServerId(serverId);
        invoices.setServiceId(serviceId);
        invoices.setMerchantId(merchantId);
        return invoices;
    }
}
