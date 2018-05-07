package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.BlockingRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for
 * blocking code that produces or receives data.
 * <p>
 * For those who know RxJava:
 * - RxJava subscribeOn = Reactor subscribeOn
 * - RxJava observeOn = Reactor publishOn
 * - RxJava Schedulers.io <==> Reactor Schedulers.elastic
 *
 * @author Sebastien Deleuze
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactive {

	//========================================================================================

	// TODO Create a Flux for reading all users from the blocking repository deferred until the flux is subscribed, and run it with an elastic scheduler
	Flux<User> blockingRepositoryToFlux(BlockingRepository<User> repository) {
		// Flux.defer is necessary. Since without it, repository.findAll() will exec immediately.

		// The reason that we use subscribeOn in here since the stage of using the data emitted by a Publisher is to
		// subscribe

		return Flux.defer(() -> Flux.fromIterable(repository.findAll()))
				.subscribeOn(Schedulers.newElastic("my-elastic"));
	}

	//========================================================================================

	// TODO Insert users contained in the Flux parameter in the blocking repository using an elastic scheduler and return a Mono<Void> that signal the end of the operation
	Mono<Void> fluxToBlockingRepository(Flux<User> flux, BlockingRepository<User> repository) {
		// The reason that we use publishOn in here since doOnNext is part of publish, and publishOn must before the
		// operator which you want it to run in a new execution context.

		return flux.publishOn(Schedulers.elastic()).doOnNext(repository::save).then();
	}

}
