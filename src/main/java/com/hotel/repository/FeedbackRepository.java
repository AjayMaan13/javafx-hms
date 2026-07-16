package com.hotel.repository;

import com.hotel.model.Feedback;

import java.util.UUID;

public class FeedbackRepository extends BaseRepository<Feedback, UUID> {

    public FeedbackRepository() {
        super(Feedback.class);
    }
}
