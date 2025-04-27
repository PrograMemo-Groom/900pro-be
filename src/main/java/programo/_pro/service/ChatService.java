package programo._pro.service;

import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.dto.chatDto.ChatMessageRequest;
import programo._pro.dto.chatDto.ChatMessageResponse;
import programo._pro.entity.*;
import programo._pro.global.exception.chatException.NotFoundChatException;
import programo._pro.global.exception.teamException.TeamException;
import programo._pro.repository.ChatRoomRepository;
import programo._pro.repository.ChatbotRepository;
import programo._pro.repository.MessageRepository;
import programo._pro.repository.TeamRepository;
import programo._pro.repository.TestRepository;
import programo._pro.repository.TestProblemRepository;
import programo._pro.repository.UserRepository;
//import programo._pro.service.chatredis.ChatPublisherService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final ChatbotRepository chatbotRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TestRepository testRepository;
    private final TestProblemRepository testProblemRepository;
//    private final ChatPublisherService chatPublisherService;

    // 팀이 생성되면 자동으로 채팅방 생성
    public void createChatRoom(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamException::NotFoundTeamException);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setTeam(team);
        chatRoomRepository.save(chatRoom);
    }

    @Transactional
    public List<ChatMessageResponse> getAllMessages(Long chatRoomId) {
        // 사용자 메시지 조회
        List<Message> messages = messageRepository.findByChatRoom_IdOrderBySendAtAsc(chatRoomId);
        // 챗봇 메시지 조회
        List<Chatbot> chatbots = chatbotRepository.findByTeam_Id(chatRoomId);

        // 사용자 메시지 + 챗봇 메시지 가져오기
        List<ChatMessageResponse> chatMessages = messages.stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getChatRoom().getId(),
                        message.getUser().getId(),
                        message.getUser().getUsername(),
                        message.getContent(),
                        message.getSendAt(),
                        false,      // 사용자 메시지는 isChatbot = false
                        null,       // 챗봇 관련 정보는 null
                        null))      // 챗봇 관련 메시지 내용은 null
                .collect(Collectors.toList());

        // 챗봇 메시지 추가
        chatbots.forEach(chatbot -> {
            chatMessages.add(new ChatMessageResponse(
                    chatbot.getId(),
                    chatbot.getTeamId(),
                    null,  // 챗봇에는 사용자 ID가 없으므로 null
                    "Chatbot",  // 고정값: 챗봇 메시지
                    chatbot.getMessage(),
                    chatbot.getSendAt(),
                    true,       // 챗봇 메시지는 isChatbot = true
                    chatbot.getTestDateTime(),  // 챗봇 메시지의 시험 날짜
                    chatbot.getMessage())); // 챗봇 메시지 내용
        });

        // 메시지 시간순으로 정렬
        chatMessages.sort((msg1, msg2) -> {
            if (msg1.getSendAt() == null && msg2.getSendAt() == null) {
                return 0;
            } else if (msg1.getSendAt() == null) {
                return 1;
            } else if (msg2.getSendAt() == null) {
                return -1;
            }
            return msg1.getSendAt().compareTo(msg2.getSendAt());
        });

        return chatMessages;
    }


    // 날짜별 메시지 조회
    @Transactional
    public List<ChatMessageResponse> getMessagesByDate(Long chatRoomId, LocalDateTime start, LocalDateTime end) {
        List<Message> messages = messageRepository.findByChatRoom_IdAndSendAtBetween(chatRoomId, start, end);
        return messages.stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getChatRoom().getId(),
                        message.getUser().getId(),
                        message.getUser().getUsername(),
                        message.getContent(),
                        message.getSendAt(),
                        false,      // 사용자 메시지는 isChatbot = false
                        null,       // 챗봇 관련 정보는 null
                        null))      // 챗봇 관련 메시지 내용은 null
                .collect(Collectors.toList());
    }

    // 키워드로 메시지 검색
    @Transactional
    public List<ChatMessageResponse> searchMessagesByKeyword(Long chatRoomId, String keyword) {
        // 검색된 메시지를 최신순으로 가져오기
        List<Message> messages = messageRepository.findByChatRoom_IdAndContentContainingOrderBySendAtDesc(chatRoomId, keyword);
        return messages.stream()
                .map(message -> {
                    // 키워드 강조
                    String highlightedContent = message.getContent().replaceAll("(?i)" + keyword, "<span style='color:yellow;'>" + keyword + "</span>");
                    return new ChatMessageResponse(
                            message.getId(),
                            message.getChatRoom().getId(),
                            message.getUser().getId(),
                            message.getUser().getUsername(),
                            highlightedContent,
                            message.getSendAt(),
                            false,
                            null,
                            null
                    );
                })
                .collect(Collectors.toList());
    }

    // 사용자가 채팅 메시지 전송
    @Transactional
    public ChatMessageResponse processUserMessage(ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(NotFoundChatException::NotFoundChatRoomException);

        //userId로 영속 객체 직접 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 메시지 생성 및 저장
        Message message = new Message();
        message.setChatRoom(chatRoom);
        message.setUser(user);
        message.setContent(request.getContent());
        message.setSendAt(LocalDateTime.now());

        messageRepository.save(message);

//
//        // Redis에 채팅 메시지 발행
//        chatPublisherService.publishMessage(chatRoom.getId().toString(), request.getContent());

        // 메시지 전송
        ChatMessageResponse response = new ChatMessageResponse(
                message.getId(),
                chatRoom.getId(),
                user.getId(),
                user.getUsername(),
                message.getContent(),
                message.getSendAt(),
                false,
                null,
                null
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), response);
        return response;
    }

    //	@Scheduled(cron = "0 0/5 * * * ?")  // 매 5분마다 실행
//    @Scheduled(cron = "0 * * * * *") // 매 분마다 실행 (테스트 용)
//    @Transactional
//    public void scheduleChatbotMessage() {
//        List<Team> teams = teamRepository.findAll();  // 모든 팀 가져오기
//        ZoneId seoulZone = ZoneId.of("Asia/Seoul");  // 서울 시간대 설정
//
//        for (Team team : teams) {
//            String testStartTime_String = team.getStartTime();
//            LocalTime startTime = LocalTime.parse(testStartTime_String, DateTimeFormatter.ofPattern("HH:mm"));
//            LocalTime nowTime = LocalTime.now(seoulZone);
//
//
//            boolean isTargetTime = nowTime.isAfter(startTime) && nowTime.isBefore(startTime.plusMinutes(5));
//            boolean notSent = !team.isChatSent();
//
//
//            log.info("Checking if team {} is already sent at {}", team.getId(), startTime);
//            if (isTargetTime && notSent) {
//                log.debug("[팀 처리] {}팀 시험 시작. 챗봇 메시지 전송", team.getTeamName());
//
//                // 메시지 전송
//                sendChatbotMessageToChatRoom(team.getId());
//
//                // 플래그 true로 설정하여 중복 전송 방지
//                team.setChatSent(true);
//                teamRepository.save(team); // 반드시 저장
//            }
//        }
//    }

    @Scheduled(cron = "6 * * * * *") // 매 분 6초대 마다 실행
    @Transactional
    public void scheduleChatbotMessage() {
        List<Team> teams = teamRepository.findAll();
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");

        for (Team team : teams) {
            if (!team.isActive()) {
                continue; // 비활성 팀이면 건너뛰어야함
            }
            String testStartTime_String = team.getStartTime(); // "HH:mm" 형태
            LocalTime startLocalTime = LocalTime.parse(testStartTime_String, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDate today = LocalDate.now(seoulZone);
            LocalDateTime startTime = LocalDateTime.of(today, startLocalTime);

            LocalDateTime now = LocalDateTime.now(seoulZone);

            boolean isAfter3Seconds = now.isAfter(startTime.plusSeconds(5));
            boolean isBefore5Minutes = now.isBefore(startTime.plusMinutes(5));
            boolean notSent = !team.isChatSent();

            log.info("Checking if team {} is already sent at {}", team.getId(), startTime);

            if (isAfter3Seconds && isBefore5Minutes && notSent) {
                log.debug("[팀 처리] {}팀 시험 시작 3초 후. 챗봇 메시지 전송", team.getTeamName());

                sendChatbotMessageToChatRoom(team.getId());

                team.setChatSent(true);
                teamRepository.save(team);
            }
        }
    }


    // 챗봇 메시지 전송 (채팅방에 맞게 챗봇 메시지를 전송)
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void sendChatbotMessageToChatRoom(Long chatRoomId) {

        // chatRoomId를 이용해 팀을 찾아야 합니다.
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(NotFoundChatException::NotFoundChatRoomException);

        Team team = chatRoom.getTeam();  // 해당 채팅방에 소속된 팀을 가져옵니다.

        // 시험 시작 시간 체크 (만약 시험 시작 시간이 지나지 않았다면 메시지 전송하지 않음)
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");  // 서울 시간대 설정
        LocalDateTime nowInSeoul = LocalDateTime.now(seoulZone);

        String testStartTime_String = team.getStartTime();  // 팀의 시험 시작 시간
        // 오늘 날짜
        LocalDate today = LocalDate.now();

        // 문자열을 LocalTime으로 파싱
        LocalTime localTime = LocalTime.parse(testStartTime_String, DateTimeFormatter.ofPattern("HH:mm"));

        // 오늘 날짜 + 시간 결합
        LocalDateTime testStartTime = LocalDateTime.of(today, localTime);

        if (nowInSeoul.isBefore(testStartTime)) {
            log.debug("[팀 처리] 팀 {}의 시험 시작 시간이 아직 되지 않았습니다.", team.getTeamName());
            return;  // 시험이 시작되지 않았다면 메서드 종료
        }

        log.debug("[팀 처리] 팀 {}의 시험이 시작되었거나 종료되었습니다.", team.getTeamName());

        createAndSendChatbotMessage(team);
    }


    // 공통적인 챗봇 메시지 생성 및 전송 형식
    private void createAndSendChatbotMessage(Team team) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        LocalDate today = now.toLocalDate();

        Test test = testRepository.findByTeam_IdAndCreatedAt(team.getId(), today)
                .orElseThrow(() -> new RuntimeException("오늘 생성된 테스트를 찾을 수 없습니다."));

        List<TestProblem> testProblems = testProblemRepository.findByTest_Id(test.getId());

        // 문제들의 baekNum을 뽑아서 메시지 만들기
        StringBuilder messageContent = new StringBuilder("응시하느라 고생하셨습니다 \n 오늘의 문제 번호: ");
        for (TestProblem testProblem : testProblems) {
            int baekNum = testProblem.getProblem().getBaekNum();
            messageContent.append(baekNum).append(" ");
        }

//        // 메시지 생성
//        StringBuilder messageContent = new StringBuilder("응시하느라 고생하셨습니다 \n 오늘의 문제 번호: ");
//        for (int i = 1; i <= team.getProblemCount(); i++) {
//            messageContent.append(i).append(" ");
//        }

        // 챗봇메시지 생성
        Chatbot chatbot = Chatbot.builder()
                .team(team)
                .testDateTime(now)         // 시험 시작 시간
                .message(messageContent.toString()) // 메세지
                .build();

        // DB 저장 (sendAt은 @PrePersist로 자동 입력)
        chatbotRepository.save(chatbot);

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByTeam_Id(team.getId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // WebSocket 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + chatRoom.getId(), chatbot.getMessage());

        log.debug("[메시지 전송] teamId={} 메시지: {}", team.getId(), chatbot.getMessage());
    }
}
