package co.gadder.gadder;

import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.gadder.gadder.emoji.Activity;
import co.gadder.gadder.emoji.Food;
import co.gadder.gadder.emoji.Objects;
import co.gadder.gadder.emoji.People;
import co.gadder.gadder.emoji.Symbols;
import co.gadder.gadder.emoji.Travel;


public class GadderActivities {

    public static class GadderActivity {
        public GadderActivity(String type, int description, String emoji) {
            this.type = type;
            this.emoji = emoji;
            this.description = description;
        }

        public String type;
        public String emoji;
        public @StringRes int description;
    }


    // Activity types
    public static final GadderActivity RECENT = new GadderActivity("Recent", R.string.recent, Objects.ALARM_CLOCK);
    public static final GadderActivity GAMES = new GadderActivity("Games", R.string.games, Activity.DIRECT_HIT);
    public static final GadderActivity MUSIC = new GadderActivity("Music", R.string.music, Symbols.MUSICAL_NOTE);
    public static final GadderActivity SPORTS = new GadderActivity("Sports", R.string.sports, Activity.RUNNING_SHIRT_WITH_SASH);
    public static final GadderActivity NIGHTLIFE = new GadderActivity("Nightlife", R.string.nightlife, Food.COCKTAIL_GLASS);
    public static final GadderActivity PLACES = new GadderActivity("Places", R.string.places, Objects.TRIANGULAR_FLAG_POST);
    public static final GadderActivity MISCELLANEOUS = new GadderActivity("Miscellaneous", R.string.miscellaneous, Objects.BALLOON);

    // Miscellaneous
    public static final GadderActivity TOILET = new GadderActivity("Toilet", R.string.toilet, Objects.TOILET);
    public static final GadderActivity SMOKING = new GadderActivity("Smoking", R.string.smoking, Objects.SMOKING_SYMBOL);
    public static final GadderActivity SHOWERING = new GadderActivity("Showering", R.string.showering, Objects.SHOWER);

    // Places
    public static final GadderActivity HOME = new GadderActivity("Home", R.string.home, Travel.HOUSE_BUILDING);
    public static final GadderActivity MOVIE = new GadderActivity("Movie", R.string.movie, Objects.MOVIE_CAMERA);
    public static final GadderActivity PARK = new GadderActivity("Park", R.string.park, Travel.NATIONAL_PARK);
    public static final GadderActivity SCHOOL = new GadderActivity("School", R.string.school, People.SCHOOL_SATCHEL);
    public static final GadderActivity SHOPPING = new GadderActivity("Shopping", R.string.shopping, Objects.SHOPPING_BAGS);
    public static final GadderActivity THEATER = new GadderActivity("Theater", R.string.theater, Activity.PERFORMING_ARTS);
    public static final GadderActivity UNIVERSITY = new GadderActivity("University", R.string.university, People.GRADUATION_CAP);
    public static final GadderActivity WORK = new GadderActivity("Work", R.string.work, People.BRIEFCASE);
    public static final GadderActivity BAR = new GadderActivity("Bar", R.string.bar, Food.BEAR_MUG);
    public static final GadderActivity PARTY = new GadderActivity("Party", R.string.party, Objects.PARTY_POPPER);

    // Sports
    public static final GadderActivity GYM = new GadderActivity("Gym", R.string.gym, People.FLEXED_BICEPS);
    public static final GadderActivity RUNNING = new GadderActivity("Running", R.string.running, People.RUNNER);
    public static final GadderActivity SURF = new GadderActivity("Surf", R.string.surf, Activity.SURFER);
    public static final GadderActivity GOLF = new GadderActivity("Golf", R.string.golf, Activity.GOLFER);
    public static final GadderActivity DANCE = new GadderActivity("Dance", R.string.dance, People.DANCER);
    public static final GadderActivity RUGBY = new GadderActivity("Rugby", R.string.rugby, Activity.RUGBY_FOOTBALL);
    public static final GadderActivity TENNIS = new GadderActivity("Tennis", R.string.tennis, Activity.TENNIS_RACQUET_BALL);
    public static final GadderActivity SOCCER = new GadderActivity("Soccer", R.string.soccer, Activity.SOCCER_BALL);
    public static final GadderActivity CYCLING = new GadderActivity("Cycling", R.string.cycling, Activity.BICYCLIST);
    public static final GadderActivity BOWLING = new GadderActivity("Bowling", R.string.bowling, Activity.BOWLING);
    public static final GadderActivity SWIMMING = new GadderActivity("Swimming", R.string.swimming, Activity.SWIMMER);
    public static final GadderActivity FOOTBALL = new GadderActivity("Football", R.string.football, Activity.AMERICAN_FOOTBALL);
    public static final GadderActivity BASEBALL = new GadderActivity("Baseball", R.string.baseball, Activity.BASEBALL);
    public static final GadderActivity BASKETBALL = new GadderActivity("Basketball", R.string.basketball, Activity.BASKET_BALL);
    public static final GadderActivity TABLE_TENNIS = new GadderActivity("Table Tennis", R.string.table_tennis, Activity.TABLE_TENNIS_PADDLE_BALL);

    // Music
    public static final GadderActivity VIOLIN = new GadderActivity("Violin", R.string.violin, Activity.VIOLIN);
    public static final GadderActivity GUITAR = new GadderActivity("Guitar", R.string.guitar, Activity.GUITAR);
    public static final GadderActivity SAXOPHONE = new GadderActivity("Sax", R.string.sax, Activity.SAXOPHONE);
    public static final GadderActivity TRUMPET = new GadderActivity("Trumpet", R.string.trumpet, Activity.TRUMPET);
    public static final GadderActivity SINGING = new GadderActivity("Singing", R.string.singing, Activity.MICROPHONE);
    public static final GadderActivity MUSICAL_KEYBOARD = new GadderActivity("Musical Keyboard", R.string.musical_keyboard, Activity.MUSICAL_KEYBOARD);

    // Games
    public static final GadderActivity CARDS = new GadderActivity("Cards", R.string.cards, Symbols.PLAYING_CARD_BLACK_JOKER);
    public static final GadderActivity RPG = new GadderActivity("RPG", R.string.rpg, Activity.GAME_DIE);
    public static final GadderActivity BILLIARDS = new GadderActivity("Billiards", R.string.billiards, Activity.BILLIARDS);
    public static final GadderActivity VIDEO_GAME = new GadderActivity("Video Game", R.string.video_game, Activity.VIDEO_GAME);

    // Food
    public static final GadderActivity BURGER = new GadderActivity("Burger", R.string.burger, Food.HAMBURGER);
    public static final GadderActivity BARBECUE = new GadderActivity("Barbecue", R.string.barbecue, Food.MEAT_ON_BONE);
    public static final GadderActivity JAPANESE_FOOD = new GadderActivity("Sushi", R.string.sushi, Food.SUSHI);

    // Hobbies
    public static final GadderActivity PAINTING = new GadderActivity("Painting", R.string.painting, Activity.ARTIST_PALETTE);


    public static final Map<String, GadderActivity> ACTIVITY_MAP;
    static {
        ACTIVITY_MAP = new HashMap<>();

        // Miscellaneous
        ACTIVITY_MAP.put(TOILET.type, TOILET);
        ACTIVITY_MAP.put(SMOKING.type, SMOKING);
        ACTIVITY_MAP.put(SHOWERING.type, SHOWERING);

        // Places
        ACTIVITY_MAP.put(HOME.type, HOME);
        ACTIVITY_MAP.put(MOVIE.type, MOVIE);
        ACTIVITY_MAP.put(PARK.type, PARK);
        ACTIVITY_MAP.put(SCHOOL.type, SCHOOL);
        ACTIVITY_MAP.put(SHOPPING.type, SHOPPING);
        ACTIVITY_MAP.put(THEATER.type, THEATER);
        ACTIVITY_MAP.put(UNIVERSITY.type, UNIVERSITY);
        ACTIVITY_MAP.put(WORK.type, WORK);
        ACTIVITY_MAP.put(BAR.type, BAR);
        ACTIVITY_MAP.put(PARTY.type, PARTY);

        // Sports
        ACTIVITY_MAP.put(GYM.type, GYM);
        ACTIVITY_MAP.put(RUNNING.type, RUNNING);
        ACTIVITY_MAP.put(SURF.type, SURF);
        ACTIVITY_MAP.put(GOLF.type, GOLF);
        ACTIVITY_MAP.put(DANCE.type, DANCE);
        ACTIVITY_MAP.put(RUGBY.type, RUGBY);
        ACTIVITY_MAP.put(TENNIS.type, TENNIS);
        ACTIVITY_MAP.put(SOCCER.type, SOCCER);
        ACTIVITY_MAP.put(CYCLING.type, CYCLING);
        ACTIVITY_MAP.put(BOWLING.type, BOWLING);
        ACTIVITY_MAP.put(SWIMMING.type, SWIMMING);
        ACTIVITY_MAP.put(FOOTBALL.type, FOOTBALL);
        ACTIVITY_MAP.put(BASEBALL.type, BASEBALL);
        ACTIVITY_MAP.put(BASKETBALL.type, BASKETBALL);
        ACTIVITY_MAP.put(TABLE_TENNIS.type, TABLE_TENNIS);

        // Music
        ACTIVITY_MAP.put(VIOLIN.type, VIOLIN);
        ACTIVITY_MAP.put(GUITAR.type, GUITAR);
        ACTIVITY_MAP.put(MUSICAL_KEYBOARD.type, MUSICAL_KEYBOARD);
        ACTIVITY_MAP.put(SAXOPHONE.type, SAXOPHONE);
        ACTIVITY_MAP.put(SINGING.type, SINGING);
        ACTIVITY_MAP.put(TRUMPET.type, TRUMPET);

        // Games
        ACTIVITY_MAP.put(CARDS.type, CARDS);
        ACTIVITY_MAP.put(RPG.type, RPG);
        ACTIVITY_MAP.put(BILLIARDS.type, BILLIARDS);
        ACTIVITY_MAP.put(VIDEO_GAME.type, VIDEO_GAME);

        // Food
        ACTIVITY_MAP.put(BURGER.type, BURGER);
        ACTIVITY_MAP.put(JAPANESE_FOOD.type, JAPANESE_FOOD);
        ACTIVITY_MAP.put(BARBECUE.type, BARBECUE);

        // Hobbies
        ACTIVITY_MAP.put(PAINTING.type, PAINTING);
    }

    public static final List<GadderActivity> ACTIVITY_TYPES = new ArrayList<>();
    static {
//        ACTIVITY_TYPES.add(RECENT);
        ACTIVITY_TYPES.add(GAMES);
        ACTIVITY_TYPES.add(MUSIC);
        ACTIVITY_TYPES.add(SPORTS);
//        ACTIVITY_TYPES.add(NIGHTLIFE);
        ACTIVITY_TYPES.add(PLACES);
        ACTIVITY_TYPES.add(MISCELLANEOUS);
    }

    public static final List<GadderActivity> ACTIVITY_GAMES = new ArrayList<>();
    static {
        ACTIVITY_GAMES.add(CARDS);
        ACTIVITY_GAMES.add(RPG);
        ACTIVITY_GAMES.add(BILLIARDS);
        ACTIVITY_GAMES.add(VIDEO_GAME);
    }

    public static final List<GadderActivity> ACTIVITY_MUSIC = new ArrayList<>();
    static {
        ACTIVITY_MUSIC.add(VIOLIN);
        ACTIVITY_MUSIC.add(GUITAR);
        ACTIVITY_MUSIC.add(MUSICAL_KEYBOARD);
        ACTIVITY_MUSIC.add(SAXOPHONE);
        ACTIVITY_MUSIC.add(SINGING);
        ACTIVITY_MUSIC.add(TRUMPET);
    }


    public static final List<GadderActivity> ACTIVITY_SPORTS = new ArrayList<>();
    static {
        ACTIVITY_SPORTS.add(GYM);
        ACTIVITY_SPORTS.add(RUNNING);
        ACTIVITY_SPORTS.add(SURF);
        ACTIVITY_SPORTS.add(GOLF);
        ACTIVITY_SPORTS.add(DANCE);
        ACTIVITY_SPORTS.add(RUGBY);
        ACTIVITY_SPORTS.add(TENNIS);
        ACTIVITY_SPORTS.add(SOCCER);
        ACTIVITY_SPORTS.add(CYCLING);
        ACTIVITY_SPORTS.add(BOWLING);
        ACTIVITY_SPORTS.add(SWIMMING);
        ACTIVITY_SPORTS.add(FOOTBALL);
        ACTIVITY_SPORTS.add(BASEBALL);
        ACTIVITY_SPORTS.add(BASKETBALL);
        ACTIVITY_SPORTS.add(TABLE_TENNIS);
    }


    public static final List<GadderActivity> ACTIVITY_NIGHTLIFE = new ArrayList<>();
    static {

    }

    public static final List<GadderActivity> ACTIVITY_PLACES = new ArrayList<>();
    static {
        ACTIVITY_PLACES.add(HOME);
        ACTIVITY_PLACES.add(MOVIE);
        ACTIVITY_PLACES.add(PARK);
        ACTIVITY_PLACES.add(SCHOOL);
        ACTIVITY_PLACES.add(SHOPPING);
        ACTIVITY_PLACES.add(THEATER);
        ACTIVITY_PLACES.add(UNIVERSITY);
        ACTIVITY_PLACES.add(WORK);
        ACTIVITY_PLACES.add(BAR);
        ACTIVITY_PLACES.add(PARTY);
    }

    public static final List<GadderActivity> ACTIVITY_MISCELLANEOUS = new ArrayList<>();
    static {
        ACTIVITY_MISCELLANEOUS.add(TOILET);
        ACTIVITY_MISCELLANEOUS.add(SMOKING);
        ACTIVITY_MISCELLANEOUS.add(SHOWERING);
        ACTIVITY_MISCELLANEOUS.add(BURGER);
        ACTIVITY_MISCELLANEOUS.add(BARBECUE);
        ACTIVITY_MISCELLANEOUS.add(JAPANESE_FOOD);
        ACTIVITY_MISCELLANEOUS.add(PAINTING);
    }

    public static final List<List<GadderActivity>> ACTIVITY_LIST = new ArrayList<>();
    static {
//        ACTIVITY_LIST.add(new ArrayList<GadderActivity>());
        ACTIVITY_LIST.add(ACTIVITY_GAMES);
        ACTIVITY_LIST.add(ACTIVITY_MUSIC);
        ACTIVITY_LIST.add(ACTIVITY_SPORTS);
//        ACTIVITY_LIST.add(ACTIVITY_NIGHTLIFE);
        ACTIVITY_LIST.add(ACTIVITY_PLACES);
        ACTIVITY_LIST.add(ACTIVITY_MISCELLANEOUS);
    }

}
