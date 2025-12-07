package build.extensions.oss.gradle.pluginutils.io

import java.io.FilterReader
import java.io.Reader
import java.nio.CharBuffer


/**
 * Implementation of [FilterReader] that delegates all [Reader] methods to another reader.
 */
@Suppress("LeakingThis")
abstract class DelegateReader(input: Reader) : FilterReader(input) {

    /**
     * Creates the delegate [Reader].
     *
     * @param input the input [Reader]
     * @return the delegate [Reader]
     */
    protected abstract fun createDelegateReader(input: Reader): Reader

    private val lazyDelegate = lazy(LazyThreadSafetyMode.NONE) {
        createDelegateReader(`in`)
    }

    /**
     * The delegate [Reader].
     */
    private val delegate: Reader by lazyDelegate


    override fun read(): Int =
        delegate.read()


    override fun read(cbuf: CharArray, off: Int, len: Int): Int =
        delegate.read(cbuf, off, len)


    override fun read(target: CharBuffer): Int =
        delegate.read(target)


    override fun read(cbuf: CharArray): Int =
        delegate.read(cbuf)


    override fun skip(n: Long): Long =
        delegate.skip(n)


    override fun ready(): Boolean =
        delegate.ready()


    override fun markSupported(): Boolean =
        delegate.markSupported()


    override fun mark(readAheadLimit: Int) =
        delegate.mark(readAheadLimit)


    override fun reset() =
        delegate.reset()


    override fun close() {
        if (lazyDelegate.isInitialized()) {
            delegate.close()
        } else {
            `in`.close()
        }
    }
}
