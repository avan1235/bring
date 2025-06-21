package `in`.procyk.bring

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.*

fun MainViewController(): UIViewController = object : UIViewController(nibName = null, bundle = null) {

    private lateinit var composeViewController: UIViewController

    override fun viewDidLoad() {
        super.viewDidLoad()

        composeViewController = ComposeUIViewController {
            BringApp()
        }

        // Add as child view controller
        addChildViewController(composeViewController)
        view.addSubview(composeViewController.view)
        composeViewController.didMoveToParentViewController(this)

        // Configure constraints to fill the parent view
        composeViewController.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activateConstraints(
            listOf(
                composeViewController.view.topAnchor.constraintEqualToAnchor(view.topAnchor),
                composeViewController.view.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                composeViewController.view.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
                composeViewController.view.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor)
            )
        )

        // Disable idle timer when view loads
        UIApplication.sharedApplication.setIdleTimerDisabled(true)
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)

        // Re-enable idle timer when view disappears
        UIApplication.sharedApplication.setIdleTimerDisabled(false)
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        // Clean up child view controller
        composeViewController.willMoveToParentViewController(null)
        composeViewController.view.removeFromSuperview()
        composeViewController.removeFromParentViewController()
    }
}
