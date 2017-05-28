JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        Direction.java \
        SectionManager.java \
        Section.java \
        Ai.java \
        TreasureMap.java \
        SmartHunter2.java \
        GameState.java \
        GameStateComparator.java \
        Agent.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
        $(RM) *.class