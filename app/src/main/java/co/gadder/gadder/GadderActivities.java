package co.gadder.gadder;

import android.support.annotation.StringRes;

import co.gadder.gadder.emoji.Activity;
import co.gadder.gadder.emoji.Food;
import co.gadder.gadder.emoji.Objects;
import co.gadder.gadder.emoji.People;
import co.gadder.gadder.emoji.Symbols;
import co.gadder.gadder.emoji.Travel;


public class GadderActivities {

    public static class GadderActivity {
        public GadderActivity(int id, int name, String emoji) {
            this.id =
            this.name = name;
            this.emoji = emoji;
        }

        public int id;
        public String emoji;
        public @StringRes int name;
    }


    // Activity types
    public static final GadderActivity RECENT = new GadderActivity(1, R.string.recent, Objects.ALARM_CLOCK);
    public static final GadderActivity GAMES = new GadderActivity(2, R.string.games, Activity.DIRECT_HIT);
    public static final GadderActivity MUSIC = new GadderActivity(3, R.string.music, Symbols.MUSICAL_NOTE);
    public static final GadderActivity SPORTS = new GadderActivity(4, R.string.sports, Activity.RUNNING_SHIRT_WITH_SASH);
    public static final GadderActivity NIGHTLIFE = new GadderActivity(5, R.string.nightlife, Food.COCKTAIL_GLASS);
    public static final GadderActivity PLACES = new GadderActivity(6, R.string.places, Objects.TRIANGULAR_FLAG_POST);

    // Miscellaneous
    public static final GadderActivity TOILET = new GadderActivity(7, R.string.toilet, Objects.TOILET);
    public static final GadderActivity SMOKING = new GadderActivity(8, R.string.smoking, Objects.SMOKING_SYMBOL);
    public static final GadderActivity SHOWERING = new GadderActivity(9, R.string.showering, Objects.SHOWER);

    // Places
    public static final GadderActivity HOME = new GadderActivity(10, R.string.home, Travel.HOUSE_BUILDING);
    public static final GadderActivity MOVIE = new GadderActivity(11, R.string.movie, Objects.MOVIE_CAMERA);
    public static final GadderActivity PARK = new GadderActivity(12, R.string.park, Travel.NATIONAL_PARK);
    public static final GadderActivity SCHOOL = new GadderActivity(13, R.string.school, People.SCHOOL_SATCHEL);
    public static final GadderActivity SHOPPING = new GadderActivity(14, R.string.shopping, Objects.SHOPPING_BAGS);
    public static final GadderActivity THEATER = new GadderActivity(15, R.string.theater, Activity.PERFORMING_ARTS);
    public static final GadderActivity UNIVERSITY = new GadderActivity(16,R.string.university, People.GRADUATION_CAP);
    public static final GadderActivity WORK = new GadderActivity(17, R.string.work, People.BRIEFCASE);
    public static final GadderActivity BAR = new GadderActivity(18, R.string.bar, Food.BEAR_MUG);
    public static final GadderActivity PARTY = new GadderActivity(19, R.string.party, Objects.PARTY_POPPER);

    // Sports
    public static final GadderActivity GYM = new GadderActivity(20, R.string.gym, People.FLEXED_BICEPS);
    public static final GadderActivity RUNNING = new GadderActivity(21, R.string.running, People.RUNNER);
    public static final GadderActivity SURF = new GadderActivity(22, R.string.surf, Activity.SURFER);
    public static final GadderActivity GOLF = new GadderActivity(23, R.string.golf, Activity.GOLFER);
    public static final GadderActivity DANCE = new GadderActivity(24, R.string.dance, People.DANCER);
    public static final GadderActivity RUGBY = new GadderActivity(25, R.string.rugby, Activity.RUGBY_FOOTBALL);
    public static final GadderActivity TENNIS = new GadderActivity(26, R.string.tennis, Activity.TENNIS_RACQUET_BALL);
    public static final GadderActivity SOCCER = new GadderActivity(27, R.string.soccer, Activity.SOCCER_BALL);
    public static final GadderActivity CYCLING = new GadderActivity(28, R.string.cycling, Activity.BICYCLIST);
    public static final GadderActivity BOWLING = new GadderActivity(29, R.string.bowling, Activity.BOWLING);
    public static final GadderActivity SWIMMING = new GadderActivity(30, R.string.swimming, Activity.SWIMMER);
    public static final GadderActivity FOOTBALL = new GadderActivity(31, R.string.football, Activity.AMERICAN_FOOTBALL);
    public static final GadderActivity BASEBALL = new GadderActivity(32, R.string.baseball, Activity.BASEBALL);
    public static final GadderActivity BASKETBALL = new GadderActivity(33, R.string.basketball, Activity.BASKET_BALL);
    public static final GadderActivity TABLE_TENNIS = new GadderActivity(34, R.string.table_tennis, Activity.TABLE_TENNIS_PADDLE_BALL);

    // Music
    public static final GadderActivity VIOLIN = new GadderActivity(35, R.string.violin, Activity.VIOLIN);
    public static final GadderActivity GUITAR = new GadderActivity(36, R.string.guitar, Activity.GUITAR);
    public static final GadderActivity MUSICAL_KEYBOARD = new GadderActivity(37, R.string.musical_keyboard, Activity.MUSICAL_KEYBOARD);
    public static final GadderActivity SAXOPHONE = new GadderActivity(38, R.string.sax, Activity.SAXOPHONE);
    public static final GadderActivity MICROPHONE = new GadderActivity(39, R.string.singing, Activity.MICROPHONE);
    public static final GadderActivity TRUMPET = new GadderActivity(40, R.string.trumpet, Activity.TRUMPET);

    // Games
    public static final GadderActivity CARDS = new GadderActivity(41, R.string.cards, Symbols.PLAYING_CARD_BLACK_JOKER);
    public static final GadderActivity RPG = new GadderActivity(42, R.string.rpg, Activity.GAME_DIE);
    public static final GadderActivity BILLARDS = new GadderActivity(43, R.string.billards, Activity.BILLIARDS);
    public static final GadderActivity VIDEO_GAME = new GadderActivity(44, R.string.video_game, Activity.VIDEO_GAME);

    // Food
    public static final GadderActivity BURGER = new GadderActivity(45, R.string.burger, Food.HAMBURGER);
    public static final GadderActivity JAPONESE_FOOD = new GadderActivity(46, R.string.sushi, Food.SUSHI);

    // Hobbies
    public static final GadderActivity PAINTING = new GadderActivity(47, R.string.painting, Activity.ARTIST_PALETTE);
}
