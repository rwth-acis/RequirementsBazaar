package de.rwth.dbis.acis.bazaar.service.twitter;

import java.net.URISyntaxException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import i5.las2peer.logging.L2pLogger;
import org.apache.http.client.utils.URIBuilder;

public class WeeklyNewProjectsTweetTask extends TimerTask {

    private final L2pLogger logger = L2pLogger.getInstance(WeeklyNewProjectsTweetTask.class.getName());

    private final BazaarService bazaarService;

    public WeeklyNewProjectsTweetTask(BazaarService bazaarService) {
        this.bazaarService = bazaarService;
    }

    /**
     * Schedules this task using the given timer.
     *
     * @param timer
     */
    public void schedule(Timer timer) {
        /*
         * Task is scheduled for Sunday, 16:00 every week.
         */
        OffsetDateTime nextDatePossibleInPast = OffsetDateTime.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .with(LocalTime.of(16, 0, 0));
        // check if next date is in past -> then add one week
        if (nextDatePossibleInPast.isBefore(OffsetDateTime.now())) {
            nextDatePossibleInPast = nextDatePossibleInPast.plusWeeks(1);
        }
        Date nextDate = Date.from(nextDatePossibleInPast.toInstant());

        long period = 1000L * 60L * 60L * 24L * 7; // weekly

        timer.schedule(this, nextDate, period);
        logger.info("Next weekly tweet scheduled for: " + nextDate);
    }

    @Override
    public void run() {
        logger.info("Running weekly task for tweeting new projects...");
        try {
            tweetNewProjects();
            logger.info("Weekly tweet task finished");
        } catch (Exception e) {
            logger.severe("Exception in weekly project tweet task");
            e.printStackTrace();
        }
    }

    public void tweetNewProjects() throws Exception {
        List<Project> projectsCreatedLastWeek = getProjectsCreatedInLastWeek();

        if (projectsCreatedLastWeek.isEmpty()) {
            logger.info("No new projects to tweet about this week :(");
            return; // No projects -> no tweet
        }

        if (!bazaarService.getTweetDispatcher().hasLinkedTwitterAccount()) {
            logger.info("Cannot post weekly tweet. No linked Twitter account.");
            return;
        }

        String text;
        if (projectsCreatedLastWeek.size() == 1) {
            text = buildMessageOneProject(projectsCreatedLastWeek.get(0));
        } else {
            text = buildMessageMultipleProjects(projectsCreatedLastWeek);
        }

        bazaarService.getTweetDispatcher().publishTweet(text);
    }

    private String buildMessageOneProject(Project project) {
        return "Check out our latest project '" + project.getName() + "'. " + buildProjectFrontendLink(project);
    }

    private String buildMessageMultipleProjects(List<Project> projects) {
        String latestProjectsLink;
        try {
            latestProjectsLink = new URIBuilder(bazaarService.getFrontendBaseURL())
                    .setPath("/projects")
                    .addParameter("sort", "date")
                    .addParameter("order", "d") // all except 'a' encodes descending order
                    .build().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid frontend base URL: " + bazaarService.getFrontendBaseURL());
        }

        // we can use indices 0, and 1 because we expect size >= 2 in this method
        return "This week, we welcome " + projects.size() + " new projects."
                + " Among others, '"  + projects.get(0).getName() + "'"
                + " and '"  + projects.get(1).getName() + "'."
                + " Check them out at " + latestProjectsLink;
    }

    private String buildProjectFrontendLink(Project project) {
        try {
            return new URIBuilder(bazaarService.getFrontendBaseURL()).setPath("/projects/" + project.getId()).build().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid frontend base URL: " + bazaarService.getFrontendBaseURL());
        }
    }

    private List<Project> getProjectsCreatedInLastWeek() throws Exception {
        DALFacade dalFacade = bazaarService.getDBConnection();

        // TODO use a custom query method to get projects filtered by creation date not older than a week ago!
        PaginationResult<Project> projects =  dalFacade.listPublicProjects(
                new PageInfo(0, 20, Collections.emptyMap(), Arrays.asList(
                        // IMPORTANT: this field name is defined by 'ProjectTransformer#getSortFields()'
                        new Pageable.SortField("date", "DESC")
                )), 0);

        OffsetDateTime aWeekAgo = OffsetDateTime.now().minusWeeks(1);

        return projects.getElements().stream()
                .filter(p -> p.getCreationDate().isAfter(aWeekAgo))
                .collect(Collectors.toList());
    }
}
