package org.jetbrains.plugins.textmate.language;

import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.textmate.language.syntax.selector.TextMateSelectorCachingWeigher;
import org.jetbrains.plugins.textmate.language.syntax.selector.TextMateSelectorWeigher;
import org.jetbrains.plugins.textmate.language.syntax.selector.TextMateSelectorWeigherImpl;
import org.jetbrains.plugins.textmate.language.syntax.selector.TextMateWeigh;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class TextMateScopeComparator<T> implements Comparator<T> {
  @NotNull
  private static final TextMateSelectorWeigher myWeigher = new TextMateSelectorCachingWeigher(new TextMateSelectorWeigherImpl());

  @NotNull
  private final CharSequence myScope;
  private final @NotNull Function<T, CharSequence> myScopeSupplier;

  public TextMateScopeComparator(@NotNull CharSequence scope, @NotNull Function<T, CharSequence> scopeSupplier) {
    myScope = scope;
    myScopeSupplier = scopeSupplier;
  }

  @Override
  public int compare(T first, T second) {
    return myWeigher.weigh(myScopeSupplier.apply(first), myScope)
      .compareTo(myWeigher.weigh(myScopeSupplier.apply(second), myScope));
  }

  @NotNull
  public List<T> sortAndFilter(@NotNull Collection<? extends T> objects) {
    return ContainerUtil.reverse(ContainerUtil.sorted(
      ContainerUtil.filter(objects, (Condition<T>)t -> myWeigher.weigh(myScopeSupplier.apply(t), myScope).weigh > 0), this));
  }

  @Nullable
  public T max(Collection<T> objects) {
    TextMateWeigh max = TextMateWeigh.ZERO;
    T result = null;
    for (T object : objects) {
      TextMateWeigh weigh = myWeigher.weigh(myScopeSupplier.apply(object), myScope);
      if (weigh.weigh > 0 && weigh.compareTo(max) > 0) {
        max = weigh;
        result = object;
      }
    }
    return result;
  }
}
