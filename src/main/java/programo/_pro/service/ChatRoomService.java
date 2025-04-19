package programo._pro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import programo._pro.entity.ChatRoom;
import programo._pro.entity.Team;
import programo._pro.global.exception.chatException.NotFoundChatException;
import programo._pro.repository.ChatRoomRepository;
import programo._pro.repository.TeamRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
	private final ChatRoomRepository chatRoomRepository;
	private final TeamRepository teamRepository;

	@Transactional
	public ChatRoom createChatRoom(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);

		ChatRoom chatRoom = ChatRoom.builder()
				.team(team)
				.build();
		return chatRoomRepository.save(chatRoom);
	}

	public ChatRoom getChatRoomByTeamId(Long teamId) {
		return chatRoomRepository.findByTeam_Id(teamId)
				.orElseThrow(NotFoundChatException::NotFoundChatRoomException);
	}
}