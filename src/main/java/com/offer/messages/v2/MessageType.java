package com.offer.messages.v2;

import com.offer.messages.v2.requests.StartSessionRequest;
import com.offer.messages.v2.requests.StartSnapshotRequest;
import com.offer.messages.v2.requests.StopSessionRequest;
import com.offer.messages.v2.responses.EndSnapshotMarker;
import com.offer.messages.v2.responses.StartSessionResponse;
import com.offer.messages.v2.responses.StartSnapshotResponse;

/**
 * Created by erwan on 5/10/15.
 *
 * Enumeration of the message types and their classes.
 */
public enum MessageType {
    //Requests
    START_SESSION_REQUEST(StartSessionRequest.class),
    STOP_SESSION_REQUEST(StopSessionRequest.class),
    START_SNAPSHOT_REQUEST(StartSnapshotRequest.class),

    // Responses
    START_SESSION_RESPONSE(StartSessionResponse.class),
    START_SNAPSHOT_RESPONSE(StartSnapshotResponse.class),

    // Snapshot Marker
    END_SNAPSHOT_MARKER(EndSnapshotMarker.class),

    // Error
    ERROR(Error.class)
    ;

    private Class className;

    MessageType(Class className) {
        this.className = className;
    }

    public Class getClassName() {
        return className;
    }
}
