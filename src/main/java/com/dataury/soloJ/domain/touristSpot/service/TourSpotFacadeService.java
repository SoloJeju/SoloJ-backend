package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotFacadeService {

    private final ChatRoomQueryService chatRoomQueryService;
//    private final ReviewService reviewService;

    public List<ChatRoomListItem> getChatRoomsByTouristSpot (Long contentId){
        return chatRoomQueryService.getChatRoomsByTouristSpot(contentId);
    }

//    public List<> getReviewListsByTouristSpot (Long contentId){
//        return ;
//    }
}
