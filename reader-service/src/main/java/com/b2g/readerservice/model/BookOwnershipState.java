package com.b2g.readerservice.model;

public enum BookOwnershipState {
    Owned,
//    questi dopo si riferiscono a lend o stati per shipment
    Concluded,

    Failed,
    Late,
    Revoked,
    Ongoing,
}
