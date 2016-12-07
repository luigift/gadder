package co.gadder.gadder;

import android.support.annotation.StringRes;

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
        public GadderActivity(int description, String emoji) {
            this.emoji = emoji;
            this.description = description;
        }

        public String emoji;
        public @StringRes int description;
    }

    public static final String TOILET = "Toilet";
    public static final String SMOKING = "Smoking";
    public static final String SHOWERING = "Showering";

    // Places
    public static final String HOME = "Home";
    public static final String MOVIE = "Movie";
    public static final String PARK = "Park";
    public static final String SCHOOL = "School";
    public static final String SHOPPING = "Shopping";
    public static final String THEATER = "Theater";
    public static final String UNIVERSITY = "University";
    public static final String WORK = "Work";
    public static final String BAR = "Bar";
    public static final String PARTY = "Party";

    // Sports
    public static final String GYM = "Gym";
    public static final String RUNNING = "Running";
    public static final String SURF = "Surf";
    public static final String GOLF = "Golf";
    public static final String DANCE = "Dance";
    public static final String RUGBY = "Rugby";
    public static final String TENNIS = "Tennis";
    public static final String SOCCER = "Soccer";
    public static final String CYCLING = "Cycling";
    public static final String BOWLING = "Bowling";
    public static final String SWIMMING = "Swimming";
    public static final String FOOTBALL = "Football";
    public static final String BASEBALL = "Baseball";
    public static final String BASKETBALL = "Basketball";
    public static final String TABLE_TENNIS = "Table_tennis";

    // Music
    public static final String VIOLIN = "Violin";
    public static final String GUITAR = "Guitar";
    public static final String MUSICAL_KEYBOARD = "Musical_keyboard";
    public static final String SAXOPHONE = "Saxophone";
    public static final String SINGING = "Singing";
    public static final String TRUMPET = "Trumpet";

    // Games
    public static final String CARDS = "Cards";
    public static final String RPG = "Rpg";
    public static final String BILLIARDS = "Billiards";
    public static final String VIDEO_GAME = "Video_game";

    // Food
    public static final String BURGER = "Burger";
    public static final String JAPANESE_FOOD = "Japanese_food";
    public static final String BARBECUE = "Barbecue";

    // Hobbies
    public static final String PAINTING = "Painting";

    public static class Emoji {

        // Activity types
        public static final GadderActivity RECENT = new GadderActivity(R.string.recent, Objects.ALARM_CLOCK);
        public static final GadderActivity GAMES = new GadderActivity(R.string.games, Activity.DIRECT_HIT);
        public static final GadderActivity MUSIC = new GadderActivity(R.string.music, Symbols.MUSICAL_NOTE);
        public static final GadderActivity SPORTS = new GadderActivity(R.string.sports, Activity.RUNNING_SHIRT_WITH_SASH);
        public static final GadderActivity NIGHTLIFE = new GadderActivity(R.string.nightlife, Food.COCKTAIL_GLASS);
        public static final GadderActivity PLACES = new GadderActivity(R.string.places, Objects.TRIANGULAR_FLAG_POST);
        public static final GadderActivity MISCELLANEOUS = new GadderActivity(R.string.miscellaneous, Objects.BALLOON);

        // Miscellaneous
        public static final GadderActivity TOILET = new GadderActivity(R.string.toilet, Objects.TOILET);
        public static final GadderActivity SMOKING = new GadderActivity(R.string.smoking, Objects.SMOKING_SYMBOL);
        public static final GadderActivity SHOWERING = new GadderActivity(R.string.showering, Objects.SHOWER);

        // Places
        public static final GadderActivity HOME = new GadderActivity(R.string.home, Travel.HOUSE_BUILDING);
        public static final GadderActivity MOVIE = new GadderActivity(R.string.movie, Objects.MOVIE_CAMERA);
        public static final GadderActivity PARK = new GadderActivity(R.string.park, Travel.NATIONAL_PARK);
        public static final GadderActivity SCHOOL = new GadderActivity(R.string.school, People.SCHOOL_SATCHEL);
        public static final GadderActivity SHOPPING = new GadderActivity(R.string.shopping, Objects.SHOPPING_BAGS);
        public static final GadderActivity THEATER = new GadderActivity(R.string.theater, Activity.PERFORMING_ARTS);
        public static final GadderActivity UNIVERSITY = new GadderActivity(R.string.university, People.GRADUATION_CAP);
        public static final GadderActivity WORK = new GadderActivity(R.string.work, People.BRIEFCASE);
        public static final GadderActivity BAR = new GadderActivity(R.string.bar, Food.BEAR_MUG);
        public static final GadderActivity PARTY = new GadderActivity(R.string.party, Objects.PARTY_POPPER);

        // Sports
        public static final GadderActivity GYM = new GadderActivity(R.string.gym, People.FLEXED_BICEPS);
        public static final GadderActivity RUNNING = new GadderActivity(R.string.running, People.RUNNER);
        public static final GadderActivity SURF = new GadderActivity(R.string.surf, Activity.SURFER);
        public static final GadderActivity GOLF = new GadderActivity(R.string.golf, Activity.GOLFER);
        public static final GadderActivity DANCE = new GadderActivity(R.string.dance, People.DANCER);
        public static final GadderActivity RUGBY = new GadderActivity(R.string.rugby, Activity.RUGBY_FOOTBALL);
        public static final GadderActivity TENNIS = new GadderActivity(R.string.tennis, Activity.TENNIS_RACQUET_BALL);
        public static final GadderActivity SOCCER = new GadderActivity(R.string.soccer, Activity.SOCCER_BALL);
        public static final GadderActivity CYCLING = new GadderActivity(R.string.cycling, Activity.BICYCLIST);
        public static final GadderActivity BOWLING = new GadderActivity(R.string.bowling, Activity.BOWLING);
        public static final GadderActivity SWIMMING = new GadderActivity(R.string.swimming, Activity.SWIMMER);
        public static final GadderActivity FOOTBALL = new GadderActivity(R.string.football, Activity.AMERICAN_FOOTBALL);
        public static final GadderActivity BASEBALL = new GadderActivity(R.string.baseball, Activity.BASEBALL);
        public static final GadderActivity BASKETBALL = new GadderActivity(R.string.basketball, Activity.BASKET_BALL);
        public static final GadderActivity TABLE_TENNIS = new GadderActivity(R.string.table_tennis, Activity.TABLE_TENNIS_PADDLE_BALL);

        // Music
        public static final GadderActivity VIOLIN = new GadderActivity(R.string.violin, Activity.VIOLIN);
        public static final GadderActivity GUITAR = new GadderActivity(R.string.guitar, Activity.GUITAR);
        public static final GadderActivity MUSICAL_KEYBOARD = new GadderActivity(R.string.musical_keyboard, Activity.MUSICAL_KEYBOARD);
        public static final GadderActivity SAXOPHONE = new GadderActivity(R.string.sax, Activity.SAXOPHONE);
        public static final GadderActivity SINGING = new GadderActivity(R.string.singing, Activity.MICROPHONE);
        public static final GadderActivity TRUMPET = new GadderActivity(R.string.trumpet, Activity.TRUMPET);

        // Games
        public static final GadderActivity CARDS = new GadderActivity(R.string.cards, Symbols.PLAYING_CARD_BLACK_JOKER);
        public static final GadderActivity RPG = new GadderActivity(R.string.rpg, Activity.GAME_DIE);
        public static final GadderActivity BILLIARDS = new GadderActivity(R.string.billiards, Activity.BILLIARDS);
        public static final GadderActivity VIDEO_GAME = new GadderActivity(R.string.video_game, Activity.VIDEO_GAME);

        // Food
        public static final GadderActivity BURGER = new GadderActivity(R.string.burger, Food.HAMBURGER);
        public static final GadderActivity BARBECUE = new GadderActivity(R.string.barbecue, Food.MEAT_ON_BONE);
        public static final GadderActivity JAPANESE_FOOD = new GadderActivity(R.string.sushi, Food.SUSHI);

        // Hobbies
        public static final GadderActivity PAINTING = new GadderActivity(R.string.painting, Activity.ARTIST_PALETTE);
    }

    public static final Map<String, GadderActivity> ACTIVITY_MAP;
    static {
        ACTIVITY_MAP = new HashMap<>();

        // Miscellaneous
        ACTIVITY_MAP.put(TOILET, Emoji.TOILET);
        ACTIVITY_MAP.put(SMOKING, Emoji.SMOKING);
        ACTIVITY_MAP.put(SHOWERING, Emoji.SHOWERING);

        // Places
        ACTIVITY_MAP.put(HOME, Emoji.HOME);
        ACTIVITY_MAP.put(MOVIE, Emoji.MOVIE);
        ACTIVITY_MAP.put(PARK, Emoji.PARK);
        ACTIVITY_MAP.put(SCHOOL, Emoji.SCHOOL);
        ACTIVITY_MAP.put(SHOPPING, Emoji.SHOPPING);
        ACTIVITY_MAP.put(THEATER, Emoji.THEATER);
        ACTIVITY_MAP.put(UNIVERSITY, Emoji.UNIVERSITY);
        ACTIVITY_MAP.put(WORK, Emoji.WORK);
        ACTIVITY_MAP.put(BAR, Emoji.BAR);
        ACTIVITY_MAP.put(PARTY, Emoji.PARTY);

        // Sports
        ACTIVITY_MAP.put(GYM, Emoji.GYM);
        ACTIVITY_MAP.put(RUNNING, Emoji.RUNNING);
        ACTIVITY_MAP.put(SURF, Emoji.SURF);
        ACTIVITY_MAP.put(GOLF, Emoji.GOLF);
        ACTIVITY_MAP.put(DANCE, Emoji.DANCE);
        ACTIVITY_MAP.put(RUGBY, Emoji.RUGBY);
        ACTIVITY_MAP.put(TENNIS, Emoji.TENNIS);
        ACTIVITY_MAP.put(SOCCER, Emoji.SOCCER);
        ACTIVITY_MAP.put(CYCLING, Emoji.CYCLING);
        ACTIVITY_MAP.put(BOWLING, Emoji.BOWLING);
        ACTIVITY_MAP.put(SWIMMING, Emoji.SWIMMING);
        ACTIVITY_MAP.put(FOOTBALL, Emoji.FOOTBALL);
        ACTIVITY_MAP.put(BASEBALL, Emoji.BASEBALL);
        ACTIVITY_MAP.put(BASKETBALL, Emoji.BASKETBALL);
        ACTIVITY_MAP.put(TABLE_TENNIS, Emoji.TABLE_TENNIS);

        // Music
        ACTIVITY_MAP.put(VIOLIN, Emoji.VIOLIN);
        ACTIVITY_MAP.put(GUITAR, Emoji.GUITAR);
        ACTIVITY_MAP.put(MUSICAL_KEYBOARD, Emoji.MUSICAL_KEYBOARD);
        ACTIVITY_MAP.put(SAXOPHONE, Emoji.SAXOPHONE);
        ACTIVITY_MAP.put(SINGING, Emoji.SINGING);
        ACTIVITY_MAP.put(TRUMPET, Emoji.TRUMPET);

        // Games
        ACTIVITY_MAP.put(CARDS, Emoji.CARDS);
        ACTIVITY_MAP.put(RPG, Emoji.RPG);
        ACTIVITY_MAP.put(BILLIARDS, Emoji.BILLIARDS);
        ACTIVITY_MAP.put(VIDEO_GAME, Emoji.VIDEO_GAME);

        // Food
        ACTIVITY_MAP.put(BURGER, Emoji.BURGER);
        ACTIVITY_MAP.put(JAPANESE_FOOD, Emoji.JAPANESE_FOOD);
        ACTIVITY_MAP.put(BARBECUE, Emoji.BARBECUE);

        // Hobbies
        ACTIVITY_MAP.put(PAINTING, Emoji.PAINTING);
    }

    public static final List<GadderActivity> ACTIVITY_TYPES = new ArrayList<>();
    static {
        ACTIVITY_TYPES.add(Emoji.RECENT);
        ACTIVITY_TYPES.add(Emoji.GAMES);
        ACTIVITY_TYPES.add(Emoji.MUSIC);
        ACTIVITY_TYPES.add(Emoji.SPORTS);
        ACTIVITY_TYPES.add(Emoji.NIGHTLIFE);
        ACTIVITY_TYPES.add(Emoji.PLACES);
        ACTIVITY_TYPES.add(Emoji.MISCELLANEOUS);
    }

    public static final List<GadderActivity> ACTIVITY_GAMES = new ArrayList<>();
    static {
        ACTIVITY_GAMES.add(Emoji.CARDS);
        ACTIVITY_GAMES.add(Emoji.RPG);
        ACTIVITY_GAMES.add(Emoji.BILLIARDS);
        ACTIVITY_GAMES.add(Emoji.VIDEO_GAME);
    }

    public static final List<GadderActivity> ACTIVITY_MUSIC = new ArrayList<>();
    static {
        ACTIVITY_MUSIC.add(Emoji.VIOLIN);
        ACTIVITY_MUSIC.add(Emoji.GUITAR);
        ACTIVITY_MUSIC.add(Emoji.MUSICAL_KEYBOARD);
        ACTIVITY_MUSIC.add(Emoji.SAXOPHONE);
        ACTIVITY_MUSIC.add(Emoji.SINGING);
        ACTIVITY_MUSIC.add(Emoji.TRUMPET);
    }


    public static final List<GadderActivity> ACTIVITY_SPORTS = new ArrayList<>();
    static {
        ACTIVITY_SPORTS.add(Emoji.GYM);
        ACTIVITY_SPORTS.add(Emoji.RUNNING);
        ACTIVITY_SPORTS.add(Emoji.SURF);
        ACTIVITY_SPORTS.add(Emoji.GOLF);
        ACTIVITY_SPORTS.add(Emoji.DANCE);
        ACTIVITY_SPORTS.add(Emoji.RUGBY);
        ACTIVITY_SPORTS.add(Emoji.TENNIS);
        ACTIVITY_SPORTS.add(Emoji.SOCCER);
        ACTIVITY_SPORTS.add(Emoji.CYCLING);
        ACTIVITY_SPORTS.add(Emoji.BOWLING);
        ACTIVITY_SPORTS.add(Emoji.SWIMMING);
        ACTIVITY_SPORTS.add(Emoji.FOOTBALL);
        ACTIVITY_SPORTS.add(Emoji.BASEBALL);
        ACTIVITY_SPORTS.add(Emoji.BASKETBALL);
        ACTIVITY_SPORTS.add(Emoji.TABLE_TENNIS);
    }


    public static final List<GadderActivity> ACTIVITY_NIGHTLIFE = new ArrayList<>();
    static {

    }

    public static final List<GadderActivity> ACTIVITY_PLACES = new ArrayList<>();
    static {
        ACTIVITY_PLACES.add(Emoji.HOME);
        ACTIVITY_PLACES.add(Emoji.MOVIE);
        ACTIVITY_PLACES.add(Emoji.PARK);
        ACTIVITY_PLACES.add(Emoji.SCHOOL);
        ACTIVITY_PLACES.add(Emoji.SHOPPING);
        ACTIVITY_PLACES.add(Emoji.THEATER);
        ACTIVITY_PLACES.add(Emoji.UNIVERSITY);
        ACTIVITY_PLACES.add(Emoji.WORK);
        ACTIVITY_PLACES.add(Emoji.BAR);
        ACTIVITY_PLACES.add(Emoji.PARTY);
    }

    public static final List<GadderActivity> ACTIVITY_MISCELLANEOUS = new ArrayList<>();
    static {
        ACTIVITY_MISCELLANEOUS.add(Emoji.TOILET);
        ACTIVITY_MISCELLANEOUS.add(Emoji.SMOKING);
        ACTIVITY_MISCELLANEOUS.add(Emoji.SHOWERING);
        ACTIVITY_MISCELLANEOUS.add(Emoji.BURGER);
        ACTIVITY_MISCELLANEOUS.add(Emoji.BARBECUE);
        ACTIVITY_MISCELLANEOUS.add(Emoji.JAPANESE_FOOD);
        ACTIVITY_MISCELLANEOUS.add(Emoji.PAINTING);
    }

    public static final List<List<GadderActivity>> ACTIVITY_LIST = new ArrayList<>();
    static {
        ACTIVITY_LIST.add(new ArrayList<GadderActivity>());
        ACTIVITY_LIST.add(ACTIVITY_GAMES);
        ACTIVITY_LIST.add(ACTIVITY_MUSIC);
        ACTIVITY_LIST.add(ACTIVITY_SPORTS);
        ACTIVITY_LIST.add(ACTIVITY_NIGHTLIFE);
        ACTIVITY_LIST.add(ACTIVITY_PLACES);
        ACTIVITY_LIST.add(ACTIVITY_MISCELLANEOUS);
    }

}
