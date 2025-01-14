package com.itwill.teamfourmen;

import com.itwill.teamfourmen.domain.Review;
import com.itwill.teamfourmen.domain.ReviewComments;
import com.itwill.teamfourmen.dto.person.CombinedCreditsCastDto;
import com.itwill.teamfourmen.dto.person.CombinedCreditsDto;
import com.itwill.teamfourmen.repository.ReviewDao;
import com.itwill.teamfourmen.service.PersonService;
import com.itwill.teamfourmen.web.PersonController;
import com.itwill.teamfourmen.web.ReviewController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@SpringBootTest
@Slf4j
class TeamFourmenApplicationTests {

	
	@Test
    public static void test() {
		String[] args = {"2", "2", "3"};
        int myBeads = Integer.parseInt(args[0]);
        int opponentBeads = Integer.parseInt(args[1]);
        int maxGames = Integer.parseInt(args[2]);

        int result = countLosingPossibilities(0, myBeads, opponentBeads, maxGames);
        System.out.println(result);
    }

    public static int countLosingPossibilities(int gamesPlayed, int myBeads, int opponentBeads, int maxGames) {
        // Base case: maximum games reached or one player loses all beads
        if (gamesPlayed == maxGames || myBeads == 0 || opponentBeads == 0) {
            return (myBeads == 0) ? 1 : 0;
        }

        // Initialize count of losing possibilities
        int losingPossibilities = 0;

        // Simulate each possible outcome of the current game (win, lose, draw)
        for (int i = 0; i < 3; i++) {
            // Update beads counts based on game outcome
            int newMyBeads = myBeads;
            int newOpponentBeads = opponentBeads;

            if (i == 0) {
                // Win: opponent loses a bead, I gain a bead
                newOpponentBeads--;
                newMyBeads++;
            } else if (i == 1) {
                // Lose: I lose a bead, opponent gains a bead
                newMyBeads--;
                newOpponentBeads++;
            }

            // Recursively explore the next game
            losingPossibilities += countLosingPossibilities(gamesPlayed + 1, newMyBeads, newOpponentBeads, maxGames);
        }

        return losingPossibilities;
    }


}
