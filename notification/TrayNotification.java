package notification;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

import module.Module;
import basic_gui.BasicDialog;

public class TrayNotification extends JWindow implements ActionListener, Module
{
  private static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

  private static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

  private static final int TIMEOUT = 5;

  private static TrayNotification this_;

  private Timer timer_;

  private JPanel main_;

  private int time_in_;

  private boolean busy_;

  private boolean abort_;

  private TrayNotification()
  {
    timer_ = new Timer(1000, this);
    busy_ = false;
    abort_ = false;
    time_in_ = 0;
    main_ = new JPanel();
    main_.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new BevelBorder(BevelBorder.LOWERED)));

    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration());

    this.requestFocusInWindow();
    this.add(main_);
    this.setSize(200, 100);
    this.setLocation(SCREEN_WIDTH - this.getWidth() - insets.right, SCREEN_HEIGHT - insets.bottom);

    this.setAlwaysOnTop(true);
  }

  public void setVisible(final boolean flag)
  {
    if(busy_ && flag)
    {
      return;
    }
    super.setVisible(true);

    // conduct transition
    new Thread(new Runnable()
    {
      private TrayNotification this_ = TrayNotification.this;

      public void run()
      {
        int direction;
        int destination;

        //
        // declare busy
        //
        busy_ = true;

        //
        // declare destination
        //
        if(flag)
        {
          direction = - 10;
          destination = SCREEN_HEIGHT - this_.getHeight();
        }
        else
        {
          destination = SCREEN_HEIGHT;
          direction = 10;
        }

        //
        // do transition
        //
        while(this_.getLocation().getY() != destination && ! abort_)
        {

          this_.setLocation((int) this_.getLocation().getX(), (int) this_.getLocation().getY() + direction);
          try
          {
            Thread.sleep(10);
          }
          catch(Exception e)
          {

          }
        }
        if(flag && !abort_)
        {
          timer_.start();
        }
        else
        {
          cleanUp();
        }
      }
    }).start();
  }


  private synchronized void cleanUp()
  {
    this.dispose();
    main_.removeAll();
    time_in_ = 0;
    busy_ = false;
    abort_ = false;
  }
  
  public synchronized void actionPerformed(ActionEvent e)
  {
    time_in_ ++ ;
    if(time_in_ == TIMEOUT)
    {
      timer_.stop();
      this.setVisible(false);
    }
  }

  public synchronized void forceInterrupt()
  {
    //
    // if timer is running, display is up, but close prematurely
    // if timer is not running, display is not up, but may be in transition.
    //
    if(timer_.isRunning())
    {
      timer_.stop();
      this.setVisible(false);
    }
    else
    {
      abort_ = true;
    }
  }

  public synchronized boolean isBusy()
  {
    return busy_;
  }

  public void displayNotification(JComponent p)
  {
    if(isBusy())
    {
      forceInterrupt();
    }
    //
    // wait.
    //
    while(isBusy())
    {
      try
      {
        Thread.sleep(100);
      }
      catch(InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    main_.add(p, BorderLayout.CENTER);
    this.setVisible(true);
  }

  public static TrayNotification instance()
  {
    if(this_ == null)
    {
      this_ = new TrayNotification();
    }
    return this_;
  }

  public boolean checkForUpdate()
  {
    return false;
  }

  public void update()
  {
  }

  public BasicDialog getOptionsDialog()
  {
    return null;
  }

  public String getType()
  {
    return "Plugin";
  }

  public void close()
  {
    System.out.println("CLOSE");
  }

}
