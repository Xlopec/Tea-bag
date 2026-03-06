package io.github.xlopec.tea.compose

/**
 * The different snapshot notification modes of Molecule.
 *
 * Compose uses snapshots to provide a consistent view of state. Mutations to state outside of the
 * composition are not automatically observed, and notifications of changes must be manually sent.
 * This can be achieved by registering a global write observer, for which you need at-minimum one
 * per process. Applications which use other Compose-based systems like Compose UI likely already
 * have one in place, whereas applications that only use Molecule need its automatic registering
 * of this notifier.
 *
 * On the JVM and Android, Molecule will read the `app.cash.molecule.snapshotNotifier` system
 * property in order to determine the default mechanism. The value is parsed with [enumValueOf],
 * or else defaults to [WhileActive] if not set or the property does not parse to a value.
 *
 * @see androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications
 */
public enum class SnapshotNotifierPolicy {
    /**
     * Rely on some other external system for sending snapshot change notifications.
     *
     * This should only be used if you can guarantee that someone else is listening to global
     * snapshot writes and sending apply notifications. Usually this means that some other
     * Compose-based system is being used in your application, and that it will always be
     * initialized prior to Molecule or at the same time.
     *
     * Failure to ensure someone else is sending apply notifications will result in state writes
     * not triggering additional recomposition.
     *
     * Some examples where this policy can be used:
     * - On Android, using Compose UI _before_ Molecule (e.g., even just calling `setContent { }` on
     *   an `Activity` or `ComposeView`) will start a singleton snapshot write listener and applier.
     *   If you are sure that this will _always_ happen, specifying this policy for all Molecule
     *   launches is valid.
     * - On JetBrains' Compose UI for Desktop, calling the `application { }` function is enough to
     *   ensure their singleton snapshot write listener and applier is started (you do not have to
     *   even show a window).
     */
    External,

    /**
     * Register a global snapshot write observer and send apply notifications when new writes occur
     * using a coroutine launched on the same scope as the composition. This coroutine will be
     * canceled and observer unregistered when that scope is canceled.
     */
    WhileActive,
}
