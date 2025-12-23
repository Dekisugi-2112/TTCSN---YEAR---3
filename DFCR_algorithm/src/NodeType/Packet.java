package NodeType;

/**
 * Packet chuẩn cho DFCR:
 *  - JOIN_REQ      : Sensor → Gateway (xin gia nhập cluster)
 *  - JOIN_ACK      : Gateway → Sensor (chấp nhận / từ chối)
 *  - DATA          : Sensor → CH → CH → BS (đa hop)
 *  - HELP          : Sensor thuộc UnCoSet broadcast tìm backup
 *  - HELP_REPLY    : Sensor thuộc CoSet trả lời HELP
 *  - HELLO         : Gateway gửi tin hiệu sống
 *  - CONTROL       : Các lệnh điều khiển (nếu cần)
 *
 * Mỗi packet đều có:
 *  - srcId    : ID node gửi
 *  - dstId    : ID node nhận (-1 = broadcast)
 *  - sizeBits : kích thước gói (bit)
 *  - hopCount : số hop đã đi qua
 *  - payload  : dữ liệu bổ sung (vd: CH id, năng lượng, route info)
 */
public class Packet {

    public enum MessageType  {
        JOIN_REQ,
        JOIN_ACK,
        DATA,
        HELP,
        HELP_REPLY,
        HELLO,
        HOP,
        CONTROL,
        ACK
    }

    private MessageType type;
    private int srcId;
    private int dstId;      // -1 = broadcast
    private int sizeBits;
    private int hopCount = 0;
    private Object payload; // tùy mục đích, có thể là CH id, Backup info...
    private long seqId;
    public static final int BROADCAST_ID = -1; // id của bs

    public Packet(MessageType type, int srcId, int dstId, int sizeBits, Object payload, long seqId) {
        this.type = type;
        this.srcId = srcId;
        this.dstId = dstId;
        this.sizeBits = sizeBits;
        this.payload = payload;
        this.seqId = seqId;
    }
    
    // convenient overload (no seq)
    public Packet(MessageType type, int srcId, int dstId, int sizeBits, Object payload) {
        this(type, srcId, dstId, sizeBits, payload, System.nanoTime());
    }
    
    // Thêm payload
    public Packet withPayload(Object p) {
        this.payload = p;
        return this;
    }



    public MessageType getType() {
        return type;
    }

    public int getSrcId() {
        return srcId;
    }

    public int getDstId() {
        return dstId;
    }

    public int getSizeBits() {
        return sizeBits;
    }

    public Object getPayload() {
        return payload;
    }

    public int getHopCount() {
        return hopCount;
    }

    public long getSeqId() {
        return seqId;
    }
    
    

    
    
    // --------------------------------
    // Tăng hopCount (khi forwarding)
    public void incrementHop() {
        hopCount++;
    }

    public boolean isBroadcast() {
        return dstId == -1;
    }
    
    public void setDstId(int dstId) { this.dstId = dstId; }
    public void setSizeBits(int sizeBits) { this.sizeBits = sizeBits; }
    
    public String shortInfo() {
        return String.format(
            "[%s seq=%d src=%d hop=%d]",
            type, seqId, srcId, hopCount
        );
    }


    
    
}
