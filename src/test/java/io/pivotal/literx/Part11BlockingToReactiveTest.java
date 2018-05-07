package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.BlockingUserRepository;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Iterator;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for
 * blocking code that produces or receives data.
 * <p>
 * For those who know RxJava:
 * - RxJava subscribeOn = Reactor subscribeOn
 * - RxJava observeOn = Reactor publishOn
 *
 * @author Sebastien Deleuze
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactiveTest {

	Part11BlockingToReactive workshop = new Part11BlockingToReactive();

	//========================================================================================

	@Test
	public void slowPublisherFastSubscriber() {
		BlockingUserRepository repository = new BlockingUserRepository();
		Flux<User> flux = workshop.blockingRepositoryToFlux(repository);
		assertEquals("The call to findAll must be deferred until the flux is subscribed", 0,
				repository.getCallCount());
		StepVerifier.create(flux)
				.expectNextMatches(predicateWithThreadName(User.SKYLER))
				.expectNextMatches(predicateWithThreadName(User.JESSE))
				.expectNextMatches(predicateWithThreadName(User.WALTER))
				.expectNextMatches(predicateWithThreadName(User.SAUL))
				.verifyComplete();
	}

	private Predicate<User> predicateWithThreadName(User expectUser) {
		return (User u) -> {
			System.out.println(Thread.currentThread().getName() + " :: doNext -> " + u.getUsername());
			return expectUser.equals(u);
		};
	}

	//========================================================================================

	@Test
	public void fastPublisherSlowSubscriber() {
		ReactiveRepository<User> reactiveRepository = new ReactiveUserRepository();
		BlockingUserRepository blockingRepository = new BlockingUserRepository(new User[]{});
		Mono<Void> complete = workshop.fluxToBlockingRepository(reactiveRepository.findAll(), blockingRepository);
		assertEquals(0, blockingRepository.getCallCount());
		StepVerifier.create(complete)
				.verifyComplete();
		Iterator<User> it = blockingRepository.findAll().iterator();
		assertEquals(User.SKYLER, it.next());
		assertEquals(User.JESSE, it.next());
		assertEquals(User.WALTER, it.next());
		assertEquals(User.SAUL, it.next());
		assertFalse(it.hasNext());
	}
}
